package com.robloxcloudly.game;

import com.robloxcloudly.Defs;
import com.robloxcloudly.model.Displays;
import com.robloxcloudly.model.PetModels;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Serce moda: lobby, dołączanie do map, bieżnie, monety, wyklucia, hoverboard. */
public final class GameManager {
    private GameManager() {}

    public static final Logger LOGGER = LoggerFactory.getLogger("robloxcloudly");
    public static final ResourceKey<Level> LOBBY = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("robloxcloudly", "lobby"));
    public static final ResourceKey<Level> STEAL = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("robloxcloudly", "steal_egg"));
    public static final int MAPS_COUNT = 100;
    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath("robloxcloudly", "speed");

    public static MinecraftServer server;
    public static final Map<Integer, MapRuntime> MAPS = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_MAP = new HashMap<>();
    private static final Map<String, Long> COOLDOWN = new HashMap<>();
    private static final List<long[]> LATER_TIMES = new ArrayList<>();
    private static final List<Runnable> LATER_TASKS = new ArrayList<>();
    private static final Set<UUID> HOVER = new HashSet<>();
    private static final Map<UUID, Entity> HOVER_BOARD = new HashMap<>();
    private static long ticks;

    public static long ticks() { return ticks; }
    public static ServerLevel lobby() { return server.getLevel(LOBBY); }
    public static ServerLevel steal() { return server.getLevel(STEAL); }
    public static ServerPlayer player(UUID u) { return server == null ? null : server.getPlayerList().getPlayer(u); }
    public static Integer mapOf(UUID u) { return PLAYER_MAP.get(u); }

    public static boolean isOurs(Level l) { return l.dimension().equals(LOBBY) || l.dimension().equals(STEAL); }

    public static void command(String cmd) {
        if (server == null) return;
        try {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), cmd);
        } catch (Exception e) {
            LOGGER.warn("[robloxcloudly] Komenda nie zadziałała: {}", cmd.length() > 200 ? cmd.substring(0, 200) + "..." : cmd, e);
        }
    }

    public static void later(int delay, Runnable r) {
        LATER_TIMES.add(new long[]{ticks + delay});
        LATER_TASKS.add(r);
    }

    // ================= start / stop =================
    public static void onServerStarted(MinecraftServer s) {
        server = s;
        MAPS.clear();
        PLAYER_MAP.clear();
        if (lobby() == null || steal() == null) {
            LOGGER.error("[robloxcloudly] Brak wymiarów lobby/steal_egg – sprawdź czy mod jest poprawnie zainstalowany.");
            server = null;
            return;
        }
        command("execute in " + Layout.DIM + " run forceload remove all");
        command("kill @e[tag=rc_dyn]");
        command("execute in " + Lobby.DIM + " run forceload add -80 -80 79 79");
        if (!lobby().getBlockState(new BlockPos(0, 100, 0)).is(Blocks.GRASS_BLOCK)) buildLobby();
        LOGGER.info("[robloxcloudly] Gotowe! Lobby + {} map Steal an Egg.", MAPS_COUNT);
    }

    public static void buildLobby() {
        BuildQueue q = new BuildQueue();
        Lobby.build(q);
        q.step(lobby(), Integer.MAX_VALUE);
    }

    public static void onServerStopping() {
        command("kill @e[tag=rc_dyn]");
        PlayerData.saveAll();
        MAPS.clear();
        PLAYER_MAP.clear();
        LATER_TIMES.clear();
        LATER_TASKS.clear();
        HOVER.clear();
        HOVER_BOARD.clear();
        server = null;
    }

    // ================= tick =================
    public static void tick(MinecraftServer s) {
        if (server == null) return;
        ticks++;
        for (MapRuntime m : new ArrayList<>(MAPS.values())) m.tick();
        for (ServerPlayer p : new ArrayList<>(server.getPlayerList().getPlayers())) {
            try { playerTick(p); } catch (Exception e) { LOGGER.warn("[robloxcloudly] błąd ticku gracza", e); }
        }
        if (!LATER_TASKS.isEmpty()) {
            List<Runnable> run = new ArrayList<>();
            Iterator<long[]> it = LATER_TIMES.iterator();
            Iterator<Runnable> rt = LATER_TASKS.iterator();
            while (it.hasNext()) {
                long[] t = it.next();
                Runnable r = rt.next();
                if (t[0] <= ticks) { run.add(r); it.remove(); rt.remove(); }
            }
            for (Runnable r : run) r.run();
        }
        if (ticks % 1200 == 0) PlayerData.saveAll();
    }

    private static void playerTick(ServerPlayer p) {
        Level lvl = p.level();
        boolean inSteal = lvl.dimension().equals(STEAL);
        boolean inLobby = lvl.dimension().equals(LOBBY);
        UUID id = p.getUUID();
        Integer idx = PLAYER_MAP.get(id);
               if (idx != null && !inSteal) {
            MapRuntime wm = MAPS.get(idx);
            if (wm == null || (wm.ready && !wm.waiting.contains(id))) { leave(p); idx = null; }
        }
        tickHover(p, inLobby || inSteal);
        if ((inLobby || inSteal) && ticks % 100 == 0) p.getFoodData().setFoodLevel(20);
        if (inLobby) {
            if (p.getY() < 60) teleport(p, Lobby.DIM, Lobby.SPAWN_X, Lobby.SPAWN_Y, Lobby.SPAWN_Z, 180.0F);
            if (ticks % 40 == 0) Items2.removeEggStacks(p);
        }
        if (!inSteal) {
            if (ticks % 20 == 0) applySpeed(p, false);
            return;
        }
        if (idx == null) {
            if (ticks % 20 == 0) toLobby(p);
            return;
        }
        MapRuntime m = MAPS.get(idx);
        if (m == null || !m.ready) return;
        int f = m.farmOf(id);
        if (f < 0) return;
        int fx = Layout.fx(idx, f);
        double x = p.getX(), z = p.getZ();
        PlayerData data = PlayerData.get(id);

        if (p.getY() < 55 || Math.abs(x - m.ox) > 100) {
            teleport(p, Layout.DIM, fx + 0.5, 65, 33.5, 180.0F);
            return;
        }
        // bramy światów
        int wi = Layout.worldAt(z);
        for (int w = 1; w <= wi; w++) {
            int req = Defs.WORLDS.get(w).req();
            if (data.speed < req) {
                teleport(p, Layout.DIM, x, 65, Layout.z0(w) + 2.5, 0.0F);
                message(p, "gate", Component.literal("Zablokowane! " + Defs.WORLDS.get(w).name() + " wymaga szybkości ⚡" + req + " (masz " + (int) data.speed + "). Trenuj na bieżni!").withStyle(ChatFormatting.RED));
                return;
            }
        }
        // bieżnia
        boolean onTread = Layout.onTreadmillArea(x, z, fx) && p.getY() < 66.2;
        if (onTread) {
            double rate = Defs.TREAD[Math.min(data.tread, Defs.TREAD.length - 1)].rate();
            data.speed += rate / 20.0;
            if (ticks % 5 == 0) p.sendSystemMessage(Component.literal("TRENING! +" + rate + " ⚡/s  •  Szybkość: " + (int) data.speed).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), true);
        }
        // wyklucie
        ItemStack egg = Items2.findEgg(p);
        if (!egg.isEmpty() && Layout.inPlot(x, z, fx)) {
            if (Items2.eggMap(egg) == idx) m.hatch(p, egg);
            else Items2.removeEggStacks(p);
            egg = Items2.findEgg(p);
        }
        // zbieranie monet
        boolean onPad = Layout.onPad(x, z, fx) && p.getY() < 66.2;
        if (onPad && data.pending >= 1) {
            long v = (long) Math.floor(data.pending);
            data.coins += v;
            data.pending -= v;
            sound(p, "minecraft:entity.experience_orb.pickup", 1.2F);
            p.sendSystemMessage(Component.literal("+" + Defs.fmt(v) + " monet!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
        }
        // co sekundę: dochód + HUD
        if (ticks % 20 == 0) {
            long inc = data.income();
            data.pending += inc;
            applySpeed(p, true);
            if (!onTread && !onPad) {
                if (!egg.isEmpty()) {
                    Defs.PetDef d = Defs.get(Items2.eggPet(egg));
                    int dist = (int) Math.sqrt((x - fx) * (x - fx) + (z - 8) * (z - 8));
                    p.sendSystemMessage(Component.literal("Niesiesz " + d.name() + " Egg – biegnij do farmy! (" + dist + " m)").withStyle(Defs.RFMT[d.rarity()], ChatFormatting.BOLD), true);
                } else {
                    p.sendSystemMessage(Component.literal("Monety: " + Defs.fmt(data.coins)).withStyle(ChatFormatting.GOLD)
                            .append(Component.literal("  |  Szybkość: ⚡" + (int) data.speed).withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal("  |  Pety: " + data.pets.size() + "/30").withStyle(ChatFormatting.GREEN))
                            .append(Component.literal("  |  +" + Defs.fmt(inc) + "/s").withStyle(ChatFormatting.DARK_GREEN))
                            .append(Component.literal("  |  Do zebrania: " + Defs.fmt(data.pending)).withStyle(ChatFormatting.AQUA)), true);
                }
            }
        }
    }

    // ================= hoverboard =================
    public static void toggleHover(ServerPlayer p) {
        UUID id = p.getUUID();
        if (HOVER.remove(id)) {
            removeBoard(id);
            message(p, "hbo", Component.literal("Hoverboard schowany.").withStyle(ChatFormatting.GRAY));
        } else {
            HOVER.add(id);
            message(p, "hbo", Component.literal("Hoverboard włączony! +35% prędkości").withStyle(ChatFormatting.AQUA));
        }
        applySpeed(p, p.level().dimension().equals(STEAL));
    }

    private static void removeBoard(UUID id) {
        HOVER_BOARD.remove(id);
        command("kill @e[tag=rc_hb_" + id.toString().substring(0, 8) + "]");
    }

    private static void tickHover(ServerPlayer p, boolean allowed) {
        UUID id = p.getUUID();
        if (!HOVER.contains(id)) return;
        if (!allowed || !Items2.has(p, "hoverboard")) {
            HOVER.remove(id);
            removeBoard(id);
            applySpeed(p, false);
            return;
        }
        Entity board = HOVER_BOARD.get(id);
        if (board == null || board.isRemoved() || board.level() != p.level()) {
            if (board != null) removeBoard(id);
            String tag = "rc_hb_" + id.toString().substring(0, 8);
            String dim = p.level().dimension().equals(STEAL) ? Layout.DIM : Lobby.DIM;
            command(Displays.summonModel(dim, p.getX(), p.getY() + 0.05, p.getZ(), p.getYRot(), PetModels.board(), 0.5,
                    Displays.tags("rc", "rc_dyn", tag), null, 1.0, 1.0, 1));
            List<Entity> list = ((ServerLevel) p.level()).getEntities((Entity) null,
                    new AABB(p.getX() - 0.4, p.getY() - 0.4, p.getZ() - 0.4, p.getX() + 0.4, p.getY() + 0.5, p.getZ() + 0.4),
                    e -> e.getType() == EntityType.BLOCK_DISPLAY && e.getPassengers().size() == PetModels.board().pieces().size());
            if (!list.isEmpty()) HOVER_BOARD.put(id, list.get(0));
            return;
        }
        board.setPos(p.getX(), p.getY() + 0.05, p.getZ());
        float yaw = p.getYRot();
        if (Math.abs(board.getYRot() - yaw) > 3) {
            board.setYRot(yaw);
            for (Entity e : board.getPassengers()) e.setYRot(yaw);
        }
        if (ticks % 4 == 0) command(String.format(Locale.ROOT, "execute as %s at @s run particle minecraft:end_rod ~ ~0.1 ~ 0.3 0 0.3 0 2", id));
    }

    // ================= gracze =================
    public static void onJoin(ServerPlayer p) {
        if (server == null) return;
        PlayerData.get(p.getUUID());
        later(5, () -> {
            if (p.isRemoved()) return;
            toLobby(p);
            p.sendSystemMessage(Component.literal("☁ Witaj na robloxcloudly.gg! Kliknij skina na podium, aby zagrać w Steal an Egg.").withStyle(ChatFormatting.AQUA));
        });
    }

    public static void onDisconnect(ServerPlayer p) {
        leave(p);
        Items2.removeEggStacks(p);
        HOVER.remove(p.getUUID());
        removeBoard(p.getUUID());
        PlayerData.unload(p.getUUID());
    }

    public static void onRespawn(ServerPlayer p) {
        if (server == null) return;
        later(2, () -> {
            Integer idx = PLAYER_MAP.get(p.getUUID());
            MapRuntime m = idx == null ? null : MAPS.get(idx);
            if (m != null && m.ready) m.enter(p);
            else toLobby(p);
        });
    }

    public static void join(ServerPlayer p, int idx) {
        if (server == null || idx < 0 || idx >= MAPS_COUNT) return;
        Integer cur = PLAYER_MAP.get(p.getUUID());
        if (cur != null && cur == idx) { toFarm(p); return; }
        leave(p);
        MapRuntime m = MAPS.computeIfAbsent(idx, MapRuntime::new);
        int farm = m.freeFarm();
        if (farm < 0) {
            p.sendSystemMessage(Component.literal("Mapa #" + (idx + 1) + " jest pełna (5/5)!").withStyle(ChatFormatting.RED));
            return;
        }
        m.owners[farm] = p.getUUID();
        PLAYER_MAP.put(p.getUUID(), idx);
        m.activate();
        if (m.ready) m.enter(p);
        else {
            m.waiting.add(p.getUUID());
            p.sendSystemMessage(Component.literal("Przygotowuję mapę #" + (idx + 1) + "... za chwilę cię przeniosę!").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static void leave(ServerPlayer p) {
        Integer idx = PLAYER_MAP.remove(p.getUUID());
        removeEggs(p);
        if (idx == null) return;
        MapRuntime m = MAPS.get(idx);
        if (m == null) return;
        m.removePlayer(p.getUUID());
        if (m.playerCount() == 0) m.deactivate();
        PlayerData.save(p.getUUID());
    }

    public static void toLobby(ServerPlayer p) {
        leave(p);
        teleport(p, Lobby.DIM, Lobby.SPAWN_X, Lobby.SPAWN_Y, Lobby.SPAWN_Z, 180.0F);
        adventure(p);
        giveMenu(p);
        applySpeed(p, false);
    }

    public static void toFarm(ServerPlayer p) {
        Integer idx = PLAYER_MAP.get(p.getUUID());
        MapRuntime m = idx == null ? null : MAPS.get(idx);
        if (m == null || !m.ready) {
            p.sendSystemMessage(Component.literal("Nie jesteś na żadnej mapie. Kliknij skina w lobby!").withStyle(ChatFormatting.RED));
            return;
        }
        if (!Items2.findEgg(p).isEmpty()) {
            p.sendSystemMessage(Component.literal("Nie możesz się teleportować z jajkiem!").withStyle(ChatFormatting.RED));
            return;
        }
        teleport(p, Layout.DIM, Layout.fx(idx, m.farmOf(p.getUUID())) + 0.5, 65, 33.5, 180.0F);
    }

    public static int playersOn(int idx) {
        MapRuntime m = MAPS.get(idx);
        return m == null ? 0 : m.playerCount();
    }

    public static int quickMap() {
        int best = -1, bestN = -1;
        for (int i = 0; i < MAPS_COUNT; i++) {
            int n = playersOn(i);
            if (n < 5 && n > bestN) { best = i; bestN = n; }
        }
        return Math.max(best, 0);
    }

    /** Kliknięcie PPM w jakąś encję (strefa interakcji). */
    public static boolean onInteract(ServerPlayer p, Entity e) {
        Level l = p.level();
        if (l.dimension().equals(LOBBY)) {
            if (dist2(e, Lobby.NPC_X, Lobby.NPC_Z) < 6.0) { Menus.openServers(p, 0); return true; }
            if (dist2(e, Lobby.BOARD_X, Lobby.BOARD_Z) < 6.0) { giveHoverboard(p); return true; }
            return false;
        }
        if (l.dimension().equals(STEAL) && e.getType() == EntityType.INTERACTION) {
            Integer idx = PLAYER_MAP.get(p.getUUID());
            if (idx == null) return true;
            MapRuntime m = MAPS.get(idx);
            if (m == null || !m.ready) return true;
            int slot = m.slotNear(e.getX(), e.getZ());
            if (slot < 0) return true;
            if (!Items2.findEgg(p).isEmpty()) {
                message(p, "one", Component.literal("Możesz nieść tylko jedno jajko naraz!").withStyle(ChatFormatting.RED));
                return true;
            }
            m.take(p, slot);
            return true;
        }
        return false;
    }

    private static double dist2(Entity e, double x, double z) {
        double dx = e.getX() - x, dz = e.getZ() - z;
        return dx * dx + dz * dz;
    }

    // ================= pomocnicze =================
    public static void teleport(ServerPlayer p, String dim, double x, double y, double z, float yaw) {
        command(String.format(Locale.ROOT, "execute in %s run tp %s %.2f %.2f %.2f %.1f 0", dim, p.getUUID(), x, y, z, yaw));
    }

    public static void adventure(ServerPlayer p) {
        if (!p.isCreative() && !p.isSpectator()) command("gamemode adventure " + p.getUUID());
    }

    public static void sound(ServerPlayer p, String sound, float pitch) {
        command(String.format(Locale.ROOT, "execute as %s at @s run playsound %s master @s ~ ~ ~ 1 %.2f", p.getUUID(), sound, pitch));
    }

    public static void title(ServerPlayer p, String titleSnbt, String subSnbt) {
        command("title " + p.getUUID() + " times 5 40 10");
        command("title " + p.getUUID() + " subtitle " + subSnbt);
        command("title " + p.getUUID() + " title " + titleSnbt);
    }

    /** Usuwa jajka i oddaje je do puli mapy (np. gdy boss złapie gracza). */
    public static void removeEggs(ServerPlayer p) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (Items2.kind(s).equals("egg")) {
                MapRuntime m = MAPS.get(Items2.eggMap(s));
                if (m != null) m.release(Items2.eggSlot(s), 40);
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static void giveMenu(ServerPlayer p) {
        if (Items2.has(p, "menu")) return;
        Inventory inv = p.getInventory();
        if (inv.getItem(8).isEmpty()) inv.setItem(8, Items2.menu());
        else inv.add(Items2.menu());
    }

    public static void giveHoverboard(ServerPlayer p) {
        if (Items2.has(p, "hoverboard")) {
            message(p, "hb", Component.literal("Masz już hoverboard! PPM nim, aby go włączyć.").withStyle(ChatFormatting.AQUA));
            return;
        }
        p.getInventory().add(Items2.hoverboard());
        sound(p, "minecraft:entity.item.pickup", 1.0F);
        title(p, Displays.text("HOVERBOARD", "aqua", true), Displays.text("PPM – włącz / wyłącz deskę", "white", false));
    }

    public static void applySpeed(ServerPlayer p, boolean inSteal) {
        AttributeInstance inst = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst == null) return;
        double val = 0.0;
        if (inSteal) val += Math.min(9.0, Defs.velOf(PlayerData.get(p.getUUID()).speed) / 16.0 - 1.0);
        if (HOVER.contains(p.getUUID())) val = (1.0 + val) * 1.35 - 1.0;
        AttributeModifier old = inst.getModifier(SPEED_ID);
        if (old != null && Math.abs(old.amount() - val) < 0.01) return;
        if (old != null) inst.removeModifier(SPEED_ID);
        if (val > 0.001) inst.addTransientModifier(new AttributeModifier(SPEED_ID, val, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    /** Wiadomość na czacie, maks. raz na 3 sekundy dla danego klucza. */
    public static void message(ServerPlayer p, String key, Component msg) {
        String k = p.getUUID() + key;
        Long last = COOLDOWN.get(k);
        if (last != null && ticks - last < 60) return;
        COOLDOWN.put(k, ticks);
        p.sendSystemMessage(msg);
    }
}
