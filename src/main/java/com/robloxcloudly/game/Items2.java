package com.robloxcloudly.game;

import com.robloxcloudly.Defs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/** Przedmioty moda – zwykłe przedmioty Minecrafta z nazwą i ukrytymi danymi (gracze nie potrzebują moda). */
public final class Items2 {
    private Items2() {}

    public static ItemStack named(Item item, int count, Component name, Component... lore) {
        ItemStack s = new ItemStack(item, Math.max(1, Math.min(64, count)));
        s.set(DataComponents.ITEM_NAME, name);
        if (lore.length > 0) {
            List<Component> l = new ArrayList<>();
            for (Component c : lore) l.add(c.copy().withStyle(st -> st.withItalic(false)));
            s.set(DataComponents.LORE, new ItemLore(l));
        }
        return s;
    }

    public static void setData(ItemStack s, CompoundTag tag) { s.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); }

    public static CompoundTag data(ItemStack s) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d == null ? new CompoundTag() : d.copyTag();
    }

    public static String kind(ItemStack s) {
        if (s.isEmpty()) return "";
        return data(s).getStringOr("rc", "");
    }

    // ---------- jajko ----------
    public static ItemStack egg(String pet, int map, int slot) {
        Defs.PetDef d = Defs.get(pet);
        ItemStack s = named(Items.SNIFFER_EGG, 1, Component.literal(d.name() + " Egg").withStyle(Defs.RFMT[d.rarity()], ChatFormatting.BOLD),
                Component.literal(Defs.RAR[d.rarity()]).withStyle(Defs.RFMT[d.rarity()]),
                Component.literal("+" + Defs.fmt(Defs.income(d)) + " monet/s").withStyle(ChatFormatting.GREEN),
                Component.literal("Zanieś je na swoją farmę!").withStyle(ChatFormatting.GRAY));
        CompoundTag t = new CompoundTag();
        t.putString("rc", "egg");
        t.putString("Pet", d.name());
        t.putInt("Map", map);
        t.putInt("Slot", slot);
        setData(s, t);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, d.rarity() >= 5);
        return s;
    }

    public static String eggPet(ItemStack s) { return data(s).getStringOr("Pet", "Chicken"); }
    public static int eggMap(ItemStack s) { return data(s).getIntOr("Map", -1); }
    public static int eggSlot(ItemStack s) { return data(s).getIntOr("Slot", -1); }

    public static ItemStack findEgg(ServerPlayer p) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (kind(s).equals("egg")) return s;
        }
        return ItemStack.EMPTY;
    }

    public static void removeEggStacks(ServerPlayer p) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (kind(inv.getItem(i)).equals("egg")) inv.setItem(i, ItemStack.EMPTY);
        }
    }

    // ---------- menu ----------
    public static ItemStack menu() {
        ItemStack s = named(Items.NETHER_STAR, 1, Component.literal("☁ Menu (PPM)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                Component.literal("W lobby: wybór mapy").withStyle(ChatFormatting.GRAY),
                Component.literal("Na mapie: sklep, indeks, farma, lobby").withStyle(ChatFormatting.GRAY));
        CompoundTag t = new CompoundTag();
        t.putString("rc", "menu");
        setData(s, t);
        return s;
    }

    // ---------- hoverboard ----------
    public static ItemStack hoverboard() {
        ItemStack s = named(Items.ECHO_SHARD, 1, Component.literal("Hoverboard").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                Component.literal("PPM – włącz/wyłącz deskę (+35% prędkości)").withStyle(ChatFormatting.GRAY));
        CompoundTag t = new CompoundTag();
        t.putString("rc", "hoverboard");
        setData(s, t);
        s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return s;
    }

    public static boolean has(ServerPlayer p, String kind) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) if (kind(inv.getItem(i)).equals(kind)) return true;
        return false;
    }
}
