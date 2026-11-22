package com.robloxcloudly.game;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntConsumer;

/** Menu w skrzynce (działa u graczy bez moda) – klik = akcja, przedmiotów nie da się zabrać. */
public class GuiMenu extends ChestMenu {
    private final IntConsumer onClick;
    private final int size;

    public GuiMenu(MenuType<?> type, int id, Inventory inv, SimpleContainer container, int rows, IntConsumer onClick) {
        super(type, id, inv, container, rows);
        this.onClick = onClick;
        this.size = rows * 9;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < size && clickType == ClickType.PICKUP) {
            final int s = slotId;
            GameManager.later(1, () -> onClick.accept(s));
        }
        this.sendAllDataToRemote();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) { return true; }
}
