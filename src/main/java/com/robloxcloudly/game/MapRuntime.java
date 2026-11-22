package com.robloxcloudly.game;

import com.robloxcloudly.Defs;
import com.robloxcloudly.model.Displays;
import com.robloxcloudly.model.PetModels;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Jedna z 100 map: 5 farm, 9 światów, bossy, jajka. Działa tylko gdy ktoś na niej gra. */
public class MapRuntime {
    public final int idx;
    public final int ox;
    public final UUID[] owners = new UUID[5];
    public boolean active, ready, building;
    final Set<UUID> waiting = new HashSet<>();
    private BuildQueue queue;
    private int spawnDelay = -1;
    private final Random rnd = new Random();

    private static final int W = 9, EGGS = W * Layout.PEDS;
    private final Entity[] bosses = new Entity[W];
    private final int[] bState = new int[W], bRoar = new int[W], bWait = new int[W];
    private final double[] bTx = new double[W], bTz = new double[W];
    private final float[] bYaw = new float[W];
    private final boolean[] eggAlive = new boolean[EGGS];
    private final String[] eggPet = new String[EGGS];
    private final int[] eggTimer = new int[EGGS];

    public MapRuntime(int idx) {
        this.idx = idx;
        this.ox = Layout.ox(idx);
    }

    ServerLevel level() { return GameManager.steal(); }
    String mapTag() { return "rc_m" + idx; }
    String dynTags(String extra) { return Displays.tags("rc", "rc_dyn", mapTag(), extra); }

    public int playerCount() {
        int n = 0;
        for (UUID u : owners) if (u != null) n++;
        return n;
    }

    public int freeFarm() {
        for (int i = 0; i < 5; i++) if (owners[i] == null) return i;
        return -1;
    }

    public int farmOf(UUID u) {
        for (int i = 0; i < 5; i++) if (u.equals(owners[i])) return i;
        return -1;
    }

    // ---------------- cykl życia ----------------
    private String forceCmd(String mode) {
        String a = String.format(Locale.ROOT, "execute in %s run forceload %s %d -26 %d 46", Layout.DIM, mode, ox - 90, ox + 90);
        return a;
    }

    public void activate() {
        if (active) return;
        active = true;
        ready = false;
        GameManager.command(forceCmd("add"));
        GameManager.command(String.format(Locale.ROOT, "execute in %s run forceload add %d %d %d -24", Layout.DIM, ox - 26, Layout.z1(W - 1) - 2, ox + 26));
        boolean built = level().getBlockState(new BlockPos(ox, 63, 44)).is(Blocks.DIRT);
        if (built) {
            spawnDelay = 40;
        } else {
            building = true;
            queue = new BuildQueue();
            Layout.buildMap(queue, idx);
        }
    }

    public void deactivate() {
        GameManager.command("kill @e[tag=" + mapTag() + "]");
        GameManager.command(forceCmd("remove"));
        GameManager.command(String.format(Locale.ROOT, "execute in %s run forceload remove %d %d %d -24", Layout.DIM, ox - 26, Layout.z1(W - 1) - 2, ox + 26));
        for (int i = 0; i < W; i++) { bosses[i] = null; bState[i] = 0; }
        for (int i = 0; i < EGGS; i++) { eggAlive[i] = false; eggTimer[i] = 0; }
        active = ready = building = false;
        queue = null;
        spawnDelay = -1;
    }

    public void tick() {
        if (!active) return;
        if (building) {
            if (queue.step(level(), 12000)) {
                building = false;
                queue = null;
                spawnDelay = 20;
            } else {
                for (UUID u : waiting) {
                    ServerPlayer p = GameManager.player(u);
                    if (p != null) p.sendSystemMessage(Component.literal("Budowanie mapy #" + (idx + 1) + "... chwilka!").withStyle(ChatFormatting.YELLOW), true);
                }
            }
            return;
        }
        if (spawnDelay > 0) {
            if (--spawnDelay == 0) spawnRuntime();
            return;
        }
        if (!ready) return;
        tickBosses();
        tickEggs();
    }

    private void spawnRuntime() {
        GameManager.command("kill @e[tag=" + mapTag() + "]");
        for (int w = 0; w < W; w++) spawnBoss(w);
        for (int i = 0; i < EGGS; i++) { eggAlive[i] = false; eggTimer[i] = 1 + i % 5; }
        for (int f = 0; f < 5; f++) updateLabel(f);
        ready = true;
        for (UUID u : new ArrayList<>(waiting)) {
            ServerPlayer p = GameManager.player(u);
            if (p != null && Objects.equals(GameManager.mapOf(u), idx)) enter(p);
        }
        waiting.clear();
    }

    // ---------------- gracze ----------------
    public void enter(ServerPlayer p) {
        int f = farmOf(p.getUUID());
        if (f < 0) return;
        int fx = Layout.fx(idx, f);
        GameManager.teleport(p, Layout.DIM, fx + 0.5, 65, 33.5, 180.0F);
        GameManager.adventure(p);
        GameManager.giveMenu(p);
        spawnPets(p);
        updateLabel(f);
        GameManager.title(p, Displays.text("STEAL AN EGG", "gold", true), Displays.text("Mapa #" + (idx + 1) + " • Farma " + (f + 1), "yellow", false));
        p.sendSystemMessage(Component.literal("★ Witaj na mapie #" + (idx + 1) + "! Kradnij jajka w światach (PPM na jajko), uciekaj przed bossem i zanieś jajko na swoją farmę. Trenuj szybkość na bieżni!").withStyle(ChatFormatting.AQUA));
    }

    public void removePlayer(UUID u) {
        int f = farmOf(u);
        if (f >= 0) {
            owners[f] = null;
            GameManager.command("kill @e[tag=rc_p" + idx + "_" + f + "]");
        }
        waiting.remove(u);
        if (ready && f >= 0) updateLabel(f);
    }

    public void spawnPets(ServerPlayer p) {
        int f = farmOf(p.getUUID());
        if (f < 0 || !ready) return;
        GameManager.command("kill @e[tag=rc_p" + idx + "_" + f + "]");
        List<String> list = PlayerData.get(p.getUUID()).pets;
        for (int i = 0; i < Math.min(PlayerData.MAX_PETS, list.size()); i++) spawnPet(f, i, list.get(i));
    }

    public void spawnPet(int farm, int slot, String name) {
        Defs.PetDef d = Defs.get(name);
        int fx = Layout.fx(idx, farm);
        String label = Displays.text2(d.name() + " ", colorName(d.rarity()), "+" + Defs.fmt(Defs.income(d)) + "/s", "green");
        GameManager.command(Displays.summonModel(Layout.DIM, Layout.slotX(fx, slot) + 0.5, 65.0, Layout.slotZ(slot) + 0.5, 0.0F,
                PetModels.get(d.name(), false), 0.5, dynTags("rc_p" + idx + "_" + farm), label, 0.6, 0.6, 0));
    }

    private void updateLabel(int f) {
        int fx = Layout.fx(idx, f);
        String tag = "rc_l" + idx + "_" + f;
        GameManager.command("kill @e[tag=" + tag + "]");
        String comp;
        if (owners[f] == null) comp = Displays.text("Wolna farma", "gray", true);
        else {
            ServerPlayer p = GameManager.player(owners[f]);
            comp = Displays.text("★ Farma: " + (p != null ? p.getName().getString() : "?") + " ★", "gold", true);
        }
        for (double z : new double[]{-3.5, 20.5}) {
            GameManager.command(String.format(Locale.ROOT, "execute in %s run summon minecraft:text_display %.2f 70.6 %.2f {%s,billboard:\"center\",shadow:1b,background:1073741824,text:%s,transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[1.6f,1.6f,1.6f]}}",
                    Layout.DIM, fx + 0.5, z, dynTags(tag), comp));
        }
    }

    static String colorName(int r) {
        return switch (r) {
            case 0 -> "gray";
            case 1 -> "green";
            case 2 -> "blue";
            case 3 -> "light_purple";
            case 4 -> "gold";
            case 5 -> "red";
            case 6 -> "aqua";
            case 7 -> "dark_gray";
            default -> "yellow";
        };
    }

    // ---------------- jajka ----------------
    public int[] pedPos(int slot) {
        int w = slot / Layout.PEDS, p = slot % Layout.PEDS;
        return new int[]{ox + Layout.PED[w][p][0], Layout.PED[w][p][1]};
    }

    /** Najbliższy piedestał do pozycji (dla kliknięcia w strefę jajka). */
    public int slotNear(double x, double z) {
        int best = -1;
        double bd = 4.0;
        for (int i = 0; i < EGGS; i++) {
            int[] pp = pedPos(i);
            double d = (pp[0] + 0.5 - x) * (pp[0] + 0.5 - x) + (pp[1] + 0.5 - z) * (pp[1] + 0.5 - z);
            if (d < bd) { bd = d; best = i; }
        }
        return best;
    }

    private void tickEggs() {
        for (int i = 0; i < EGGS; i++) {
            if (!eggAlive[i] && eggTimer[i] >= 0) {
                if (eggTimer[i] > 0) eggTimer[i]--;
                else spawnEgg(i);
            }
        }
    }

    private void spawnEgg(int slot) {
        Defs.PetDef d = Defs.pick(slot / Layout.PEDS, net.minecraft.util.RandomSource.create());
        int[] pp = pedPos(slot);
        String tag = "rc_e" + idx + "_" + slot;
        String label = Displays.text2(d.name() + " Egg ", colorName(d.rarity()), "[" + Defs.RAR[d.rarity()] + "] +" + Defs.fmt(Defs.income(d)) + "/s", "green");
        GameManager.command(Displays.summonModel(Layout.DIM, pp[0] + 0.5, 66.0, pp[1] + 0.5, rnd.nextFloat() * 360.0F,
                PetModels.egg(d.world(), d.rarity()), 0.42, dynTags(tag), label, 0.7, 1.0, 0));
        GameManager.command(String.format(Locale.ROOT, "execute in %s run summon minecraft:interaction %.2f 66.0 %.2f {%s,width:1.3f,height:1.4f,response:1b}",
                Layout.DIM, pp[0] + 0.5, pp[1] + 0.5, dynTags(tag)));
        eggAlive[slot] = true;
        eggPet[slot] = d.name();
        eggTimer[slot] = 0;
    }

    /** Gracz kradnie jajko z danego slotu. */
    public void take(ServerPlayer p, int slot) {
        if (slot < 0 || slot >= EGGS || !eggAlive[slot]) return;
        String pet = eggPet[slot];
        GameManager.command("kill @e[tag=rc_e" + idx + "_" + slot + "]");
        eggAlive[slot] = false;
        eggTimer[slot] = -1;
        p.getInventory().add(Items2.egg(pet, idx, slot));
        int w = slot / Layout.PEDS;
        if (bState[w] == 0 || bState[w] == 3) { bState[w] = 1; bRoar[w] = 20; }
        Entity b = bosses[w];
        if (b != null) GameManager.command(String.format(Locale.ROOT, "execute in %s run playsound minecraft:entity.ravager.roar hostile @a %.1f %.1f %.1f 3 0.7", Layout.DIM, b.getX(), b.getY(), b.getZ()));
        Defs.PetDef d = Defs.get(pet);
        GameManager.title(p, Displays.text("UCIEKAJ!", "red", true), Displays.text(Defs.WORLDS.get(w).boss() + " cię goni! Biegnij z " + d.name() + " Egg do farmy!", "yellow", false));
    }

    /** Jajko z danego slotu wraca do puli po n tickach. */
    public void release(int slot, int ticks) {
        if (slot >= 0 && slot < EGGS && !eggAlive[slot]) eggTimer[slot] = ticks;
    }

    // ---------------- bossy ----------------
    private double homeX() { return ox + 0.5; }
    private double homeZ(int w) { return Layout.z1(w) + 18.5; }

    private void spawnBoss(int w) {
        Defs.WorldDef wd = Defs.WORLDS.get(w);
        String tag = "rc_b" + idx + "_" + w;
        GameManager.command("kill @e[tag=" + tag + "]");
        double hx = homeX(), hz = homeZ(w);
        String label = Displays.text2("BOSS: " + wd.boss() + " ", "dark_red", "⚡" + wd.bossSpeed(), "yellow");
        GameManager.command(Displays.summonModel(Layout.DIM, hx, 65.0, hz, 180.0F, PetModels.get(wd.boss(), true), 0.5 * wd.bossScale(),
                dynTags(tag), label, 1.4, 1.5, 2));
        bosses[w] = findRoot(hx, 65.0, hz);
        bState[w] = 0;
        bTx[w] = hx;
        bTz[w] = hz;
        bYaw[w] = 180.0F;
    }

    private Entity findRoot(double x, double y, double z) {
        List<Entity> list = level().getEntities((Entity) null, new AABB(x - 0.4, y - 0.4, z - 0.4, x + 0.4, y + 0.4, z + 0.4),
                e -> e.getType() == EntityType.BLOCK_DISPLAY && !e.getPassengers().isEmpty());
        return list.isEmpty() ? null : list.get(0);
    }

    private static double bossStep(int w) {
        return 4.3 * 1.3 * Defs.velOf(Defs.WORLDS.get(w).bossSpeed()) / 16.0 / 20.0;
    }

    private void moveBoss(int w, double tx, double tz, double step) {
        Entity b = bosses[w];
        double dx = tx - b.getX(), dz = tz - b.getZ(), d = Math.sqrt(dx * dx + dz * dz);
        if (d < 1.0E-3) return;
        double s = Math.min(step, d);
        b.setPos(b.getX() + dx / d * s, 65.0, b.getZ() + dz / d * s);
        face(w, (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F);
    }

    private void face(int w, float yaw) {
        float diff = Math.abs(((yaw - bYaw[w]) % 360 + 540) % 360 - 180);
        if (diff < 4) return;
        bYaw[w] = yaw;
        Entity b = bosses[w];
        b.setYRot(yaw);
        for (Entity p : b.getPassengers()) p.setYRot(yaw);
    }

    private ServerPlayer chaseTarget(int w) {
        int z0 = Layout.z0(w), z1 = Layout.z1(w);
        for (UUID u : owners) {
            if (u == null) continue;
            ServerPlayer p = GameManager.player(u);
            if (p == null) continue;
            ItemStack egg = Items2.findEgg(p);
            if (egg.isEmpty() || Items2.eggMap(egg) != idx) continue;
            if (Items2.eggSlot(egg) / Layout.PEDS != w) continue;
            if (p.getZ() <= z0 + 1.0 && p.getZ() >= z1) return p;
        }
        return null;
    }

    private void tickBosses() {
        for (int w = 0; w < W; w++) {
            Entity b = bosses[w];
            if (b == null || b.isRemoved()) {
                if (GameManager.ticks() % 100 == w) spawnBoss(w);
                continue;
            }
            ServerPlayer target = chaseTarget(w);
            double hx = homeX(), hz = homeZ(w);
            double step = bossStep(w);
            switch (bState[w]) {
                case 1 -> { // ryk
                    if (target == null) { bState[w] = 3; break; }
                    face(w, (float) (Math.atan2(target.getZ() - b.getZ(), target.getX() - b.getX()) * (180.0 / Math.PI)) - 90.0F);
                    if (--bRoar[w] <= 0) bState[w] = 2;
                }
                case 2 -> { // pościg
                    if (target == null) { bState[w] = 3; break; }
                    moveBoss(w, target.getX(), target.getZ(), step);
                    double cx = Math.max(ox - 22.0, Math.min(ox + 23.0, b.getX()));
                    double cz = Math.max(Layout.z1(w) + 1.5, Math.min(Layout.z0(w) - 0.5, b.getZ()));
                    b.setPos(cx, 65.0, cz);
                    double catchR = 1.4 + Defs.WORLDS.get(w).bossScale() * 0.5;
                    double dx = b.getX() - target.getX(), dz = b.getZ() - target.getZ();
                    if (dx * dx + dz * dz < catchR * catchR) caught(target, w);
                }
                case 3 -> { // powrót
                    if (target != null) { bState[w] = 2; break; }
                    moveBoss(w, hx, hz, step * 0.5);
                    double dx = b.getX() - hx, dz = b.getZ() - hz;
                    if (dx * dx + dz * dz < 1.0) { bState[w] = 0; bWait[w] = 40; }
                }
                default -> { // spacer
                    if (target != null) { bState[w] = 1; bRoar[w] = 20; break; }
                    if (bWait[w] > 0) { bWait[w]--; break; }
                    moveBoss(w, bTx[w], bTz[w], 0.12);
                    double dx = b.getX() - bTx[w], dz = b.getZ() - bTz[w];
                    if (dx * dx + dz * dz < 1.0) {
                        bWait[w] = 30 + rnd.nextInt(60);
                        bTx[w] = hx + rnd.nextInt(29) - 14;
                        bTz[w] = hz + rnd.nextInt(21) - 10;
                    }
                }
            }
        }
    }

    private void caught(ServerPlayer p, int w) {
        GameManager.removeEggs(p);
        GameManager.teleport(p, Layout.DIM, ox + 0.5, 65.0, Layout.z0(w) + 3.5, 0.0F);
        bState[w] = 3;
        GameManager.command("execute as " + p.getUUID() + " at @s run particle minecraft:explosion ~ ~1 ~ 0.3 0.3 0.3 0 3");
        GameManager.sound(p, "minecraft:block.anvil.land", 0.6F);
        GameManager.title(p, Displays.text("ZŁAPANY!", "dark_red", true), Displays.text("Boss zabrał jajko. Potrenuj szybkość na bieżni!", "gray", false));
    }

    // ---------------- wyklucie ----------------
    public void hatch(ServerPlayer p, ItemStack egg) {
        int f = farmOf(p.getUUID());
        if (f < 0) return;
        PlayerData data = PlayerData.get(p.getUUID());
        if (data.pets.size() >= PlayerData.MAX_PETS) {
            GameManager.message(p, "full", Component.literal("Farma pełna (30/30)! Sprzedaj pety w Indeksie (Menu).").withStyle(ChatFormatting.RED));
            return;
        }
        String pet = Items2.eggPet(egg);
        int slot = Items2.eggSlot(egg);
        Items2.removeEggStacks(p);
        release(slot, 100 + rnd.nextInt(160));
        Defs.PetDef d = Defs.get(pet);
        boolean isNew = !data.found.contains(d.name());
        data.pets.add(d.name());
        data.found.add(d.name());
        spawnPet(f, data.pets.size() - 1, d.name());
        int fx = Layout.fx(idx, f), s = data.pets.size() - 1;
        GameManager.command(String.format(Locale.ROOT, "execute in %s run particle minecraft:totem_of_undying %.1f 66 %.1f 0.6 0.8 0.6 0.3 60 force",
                Layout.DIM, Layout.slotX(fx, s) + 0.5, Layout.slotZ(s) + 0.5));
        GameManager.sound(p, "minecraft:entity.player.levelup", 1.0F);
        GameManager.title(p, Displays.text(d.name(), colorName(d.rarity()), true),
                Displays.text("Wykluł się! " + Defs.RAR[d.rarity()] + " • +" + Defs.fmt(Defs.income(d)) + "/s", "white", false));
        if (isNew) p.sendSystemMessage(Component.literal("✦ NOWY PET w Indeksie: " + d.name() + "!").withStyle(ChatFormatting.YELLOW));
        if (d.rarity() >= 6) {
            GameManager.server.getPlayerList().broadcastSystemMessage(Component.literal("★ " + p.getName().getString() + " wykluł " + Defs.RAR[d.rarity()] + ": " + d.name() + "!").withStyle(Defs.RFMT[d.rarity()], ChatFormatting.BOLD), false);
        }
        PlayerData.save(p.getUUID());
    }
}
