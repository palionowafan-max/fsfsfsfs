package com.robloxcloudly.game;

import com.robloxcloudly.Defs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

/** Wszystkie menu (w skrzynce): wybór mapy, menu główne, sklep, indeks. */
public final class Menus {
    private Menus() {}

    static MutableComponent t(String s, ChatFormatting... f) { return Component.literal(s).withStyle(f); }

    static ItemStack icon(Item item, int count, Component name, Component... lore) {
        return Items2.named(item, count, name, lore);
    }

    static PlayerData d(ServerPlayer p) { return PlayerData.get(p.getUUID()); }

    static ItemStack filler() { return icon(Items.GRAY_STAINED_GLASS_PANE, 1, Component.literal(" ")); }

    static void open(ServerPlayer p, String title, int rows, SimpleContainer c, IntConsumer click) {
        MenuType<?> type = rows == 3 ? MenuType.GENERIC_9x3 : MenuType.GENERIC_9x6;
        p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new GuiMenu(type, id, inv, c, rows, click), Component.literal(title)));
    }

    static void click(ServerPlayer p) { GameManager.sound(p, "minecraft:ui.button.click", 1.0F); }

    // ---------------- wybór mapy ----------------
    public static void openServers(ServerPlayer p, int page) {
        SimpleContainer c = new SimpleContainer(54);
        for (int i = 45; i < 54; i++) c.setItem(i, filler());
        for (int i = 0; i < 45; i++) {
            int idx = page * 45 + i;
            if (idx >= GameManager.MAPS_COUNT) break;
            int n = GameManager.playersOn(idx);
            Item it = n >= 5 ? Items.RED_WOOL : n > 0 ? Items.YELLOW_WOOL : Items.LIME_WOOL;
            c.setItem(i, icon(it, Math.max(1, n), t("Mapa #" + (idx + 1), ChatFormatting.WHITE, ChatFormatting.BOLD),
                    t("Gracze: " + n + "/5", n >= 5 ? ChatFormatting.RED : ChatFormatting.GREEN),
                    t(n >= 5 ? "Pełna!" : "Kliknij, aby dołączyć", ChatFormatting.GRAY)));
        }
        int pages = (GameManager.MAPS_COUNT + 44) / 45;
        if (page > 0) c.setItem(45, icon(Items.ARROW, 1, t("« Poprzednia strona", ChatFormatting.YELLOW)));
        if (page < pages - 1) c.setItem(53, icon(Items.ARROW, 1, t("Następna strona »", ChatFormatting.YELLOW)));
        c.setItem(49, icon(Items.NETHER_STAR, 1, t("★ Szybka gra", ChatFormatting.GOLD, ChatFormatting.BOLD), t("Dołącz do mapy z wolnym miejscem", ChatFormatting.GRAY)));
        open(p, "Steal an Egg – mapy (" + (page + 1) + "/" + pages + ")", 6, c, slot -> {
            click(p);
            if (slot < 45) {
                int idx = page * 45 + slot;
                if (idx < GameManager.MAPS_COUNT) { p.closeContainer(); GameManager.join(p, idx); }
            } else if (slot == 45 && page > 0) openServers(p, page - 1);
            else if (slot == 53 && page < pages - 1) openServers(p, page + 1);
            else if (slot == 49) { p.closeContainer(); GameManager.join(p, GameManager.quickMap()); }
        });
    }

    // ---------------- menu główne ----------------
    public static void openMain(ServerPlayer p) {
        SimpleContainer c = new SimpleContainer(27);
        for (int i = 0; i < 27; i++) c.setItem(i, filler());
        c.setItem(10, icon(Items.EMERALD, 1, t("Sklep", ChatFormatting.GREEN, ChatFormatting.BOLD), t("Lepsze bieżnie = szybszy trening", ChatFormatting.GRAY)));
        c.setItem(12, icon(Items.BOOK, 1, t("Indeks petów", ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), t("Zobacz i sprzedawaj pety", ChatFormatting.GRAY)));
        c.setItem(14, icon(Items.GRASS_BLOCK, 1, t("Teleport na farmę", ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        c.setItem(16, icon(Items.COMPASS, 1, t("Wróć do lobby", ChatFormatting.AQUA, ChatFormatting.BOLD)));
        c.setItem(22, icon(Items.PAPER, 1, t("Twoje statystyki", ChatFormatting.WHITE, ChatFormatting.BOLD),
                t("Monety: " + Defs.fmt(d(p).coins), ChatFormatting.GOLD),
                t("Szybkość: ⚡" + (int) d(p).speed, ChatFormatting.YELLOW),
                t("Pety: " + d(p).pets.size() + "/30", ChatFormatting.GREEN),
                t("Dochód: +" + Defs.fmt(d(p).income()) + "/s", ChatFormatting.DARK_GREEN),
                t("Bieżnia: " + Defs.TREAD[Math.min(d(p).tread, Defs.TREAD.length - 1)].name(), ChatFormatting.GRAY)));
        open(p, "robloxcloudly.gg – Menu", 3, c, slot -> {
            click(p);
            switch (slot) {
                case 10 -> openShop(p);
                case 12 -> openIndex(p, 0);
                case 14 -> { p.closeContainer(); GameManager.toFarm(p); }
                case 16 -> { p.closeContainer(); GameManager.toLobby(p); }
                default -> {}
            }
        });
    }

    // ---------------- sklep ----------------
    private static final Item[] TREAD_ICONS = {Items.OAK_PLANKS, Items.COBBLESTONE, Items.IRON_BLOCK, Items.GOLD_BLOCK, Items.DIAMOND_BLOCK, Items.MAGMA_BLOCK, Items.PURPUR_BLOCK};

    public static void openShop(ServerPlayer p) {
        SimpleContainer c = new SimpleContainer(27);
        for (int i = 0; i < 27; i++) c.setItem(i, filler());
        int owned = d(p).tread;
        for (int i = 0; i < Defs.TREAD.length; i++) {
            Defs.Tread tr = Defs.TREAD[i];
            Component status = i <= owned ? t("✔ Masz", ChatFormatting.GREEN)
                    : i == owned + 1 ? t("Cena: " + Defs.fmt(tr.cost()) + " monet – kliknij, aby kupić", d(p).coins >= tr.cost() ? ChatFormatting.GOLD : ChatFormatting.RED)
                    : t("Najpierw kup poprzednią", ChatFormatting.DARK_GRAY);
            c.setItem(10 + i, icon(TREAD_ICONS[i], 1, t(tr.name(), i <= owned ? ChatFormatting.GREEN : ChatFormatting.WHITE, ChatFormatting.BOLD),
                    t("+" + tr.rate() + " ⚡ szybkości na sekundę", ChatFormatting.YELLOW), status));
        }
        c.setItem(22, icon(Items.BARRIER, 1, t("« Wróć", ChatFormatting.RED)));
        c.setItem(4, icon(Items.GOLD_INGOT, 1, t("Monety: " + Defs.fmt(d(p).coins), ChatFormatting.GOLD, ChatFormatting.BOLD)));
        open(p, "Sklep – bieżnie", 3, c, slot -> {
            click(p);
            if (slot == 22) { openMain(p); return; }
            int i = slot - 10;
            if (i < 0 || i >= Defs.TREAD.length) return;
            int cur = d(p).tread;
            if (i != cur + 1) return;
            long cost = Defs.TREAD[i].cost();
            if (d(p).coins < cost) {
                p.sendSystemMessage(t("Za mało monet! Potrzebujesz " + Defs.fmt(cost) + ".", ChatFormatting.RED));
                return;
            }
            d(p).coins -= cost;
            d(p).tread = i;
            PlayerData.save(p.getUUID());
            GameManager.sound(p, "minecraft:entity.player.levelup", 1.4F);
            p.sendSystemMessage(t("Kupiono: " + Defs.TREAD[i].name() + "! Trening +" + Defs.TREAD[i].rate() + " ⚡/s", ChatFormatting.GREEN));
            openShop(p);
        });
    }

    // ---------------- indeks ----------------
    private static final Item[] RARITY_ICONS = {Items.LIGHT_GRAY_WOOL, Items.LIME_WOOL, Items.BLUE_WOOL, Items.PURPLE_WOOL, Items.ORANGE_WOOL, Items.RED_WOOL, Items.CYAN_WOOL, Items.BLACK_WOOL, Items.YELLOW_WOOL};

    private static final Item[] WORLD_ICONS = {Items.GRASS_BLOCK, Items.WATER_BUCKET, Items.SAND, Items.JUNGLE_LOG, Items.SNOW_BLOCK, Items.MAGMA_BLOCK, Items.PRISMARINE, Items.BONE_BLOCK, Items.END_STONE};

    public static void openIndex(ServerPlayer p, int world) {
        SimpleContainer c = new SimpleContainer(54);
        for (int i = 9; i < 54; i++) c.setItem(i, filler());
        for (int w = 0; w < Defs.WORLDS.size(); w++) {
            Defs.WorldDef wd = Defs.WORLDS.get(w);
            c.setItem(w, icon(WORLD_ICONS[w], 1, t((w == world ? "▶ " : "") + wd.name(), w == world ? ChatFormatting.GOLD : ChatFormatting.WHITE, ChatFormatting.BOLD),
                    t("Wymagana szybkość: ⚡" + wd.req(), ChatFormatting.YELLOW), t("Boss: " + wd.boss(), ChatFormatting.RED)));
        }
        List<String> pets = d(p).pets;
        Set<String> found = d(p).found;
        List<Defs.PetDef> eggs = Defs.WORLDS.get(world).eggs();
        for (int k = 0; k < eggs.size() && k < 8; k++) {
            Defs.PetDef d = eggs.get(k);
            int cnt = 0;
            for (String s : pets) if (s.equals(d.name())) cnt++;
            long inc = Defs.income(d);
            List<Component> lore = new ArrayList<>();
            lore.add(t(Defs.RAR[d.rarity()], Defs.RFMT[d.rarity()], ChatFormatting.BOLD));
            lore.add(t("+" + Defs.fmt(inc) + " monet/s", ChatFormatting.GREEN));
            lore.add(t("Masz: " + cnt, ChatFormatting.WHITE));
            if (cnt > 0) lore.add(t("Kliknij, aby sprzedać 1 za " + Defs.fmt(inc * 25), ChatFormatting.GOLD));
            ItemStack ic = icon(RARITY_ICONS[d.rarity()], Math.max(1, cnt), found.contains(d.name()) ? t(d.name(), Defs.RFMT[d.rarity()], ChatFormatting.BOLD) : t("???", ChatFormatting.DARK_GRAY), lore.toArray(new Component[0]));
            c.setItem(19 + k, ic);
        }
        c.setItem(49, icon(Items.PAPER, 1, t("Odkryte: " + found.size() + "/" + Defs.PETS.size(), ChatFormatting.AQUA, ChatFormatting.BOLD),
                t("Na farmie: " + pets.size() + "/30", ChatFormatting.GREEN), t("Dochód: +" + Defs.fmt(d(p).income()) + "/s", ChatFormatting.DARK_GREEN)));
        c.setItem(45, icon(Items.BARRIER, 1, t("« Wróć", ChatFormatting.RED)));
        open(p, "Indeks – " + Defs.WORLDS.get(world).name(), 6, c, slot -> {
            click(p);
            if (slot < 9 && slot < Defs.WORLDS.size()) { openIndex(p, slot); return; }
            if (slot == 45) { openMain(p); return; }
            int k = slot - 19;
            if (k < 0 || k >= eggs.size() || k >= 8) return;
            Defs.PetDef pd = eggs.get(k);
            List<String> list = d(p).pets;
            int at = list.lastIndexOf(pd.name());
            if (at < 0) return;
            list.remove(at);
            long v = Defs.income(pd) * 25;
            d(p).coins += v;
            PlayerData.save(p.getUUID());
            p.sendSystemMessage(t("Sprzedano " + pd.name() + " za " + Defs.fmt(v) + " monet.", ChatFormatting.GOLD));
            Integer idx = GameManager.mapOf(p.getUUID());
            MapRuntime m = idx == null ? null : GameManager.MAPS.get(idx);
            if (m != null && m.ready) m.spawnPets(p);
            openIndex(p, world);
        });
    }
}
