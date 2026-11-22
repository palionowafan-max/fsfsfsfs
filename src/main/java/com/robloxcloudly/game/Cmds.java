package com.robloxcloudly.game;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Komendy: /lobby /farma /sklep /indeks /mapy /graj <nr> + admin /cloudly ... */
public final class Cmds {
    private Cmds() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("lobby").executes(c -> { GameManager.toLobby(c.getSource().getPlayerOrException()); return 1; }));
        d.register(Commands.literal("farma").executes(c -> { GameManager.toFarm(c.getSource().getPlayerOrException()); return 1; }));
        d.register(Commands.literal("mapy").executes(c -> { Menus.openServers(c.getSource().getPlayerOrException(), 0); return 1; }));
        d.register(Commands.literal("graj")
                .then(Commands.argument("mapa", IntegerArgumentType.integer(1, GameManager.MAPS_COUNT))
                        .executes(c -> { GameManager.join(c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "mapa") - 1); return 1; })));
        d.register(Commands.literal("sklep").executes(c -> {
            ServerPlayer p = c.getSource().getPlayerOrException();
            if (GameManager.mapOf(p.getUUID()) == null) { p.sendSystemMessage(Component.literal("Sklep działa na mapie Steal an Egg.").withStyle(ChatFormatting.RED)); return 0; }
            Menus.openShop(p);
            return 1;
        }));
        d.register(Commands.literal("indeks").executes(c -> { Menus.openIndex(c.getSource().getPlayerOrException(), 0); return 1; }));
        d.register(Commands.literal("hoverboard").executes(c -> { GameManager.toggleHover(c.getSource().getPlayerOrException()); return 1; }));

        d.register(Commands.literal("cloudly").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("monety").then(Commands.argument("ile", LongArgumentType.longArg(0)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    PlayerData.get(p.getUUID()).coins += LongArgumentType.getLong(c, "ile");
                    c.getSource().sendSuccess(() -> Component.literal("Dodano monety."), false);
                    return 1;
                })))
                .then(Commands.literal("szybkosc").then(Commands.argument("ile", DoubleArgumentType.doubleArg(1, 100000)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    PlayerData.get(p.getUUID()).speed = DoubleArgumentType.getDouble(c, "ile");
                    c.getSource().sendSuccess(() -> Component.literal("Ustawiono szybkość."), false);
                    return 1;
                })))
                .then(Commands.literal("reset").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    PlayerData.reset(p.getUUID());
                    c.getSource().sendSuccess(() -> Component.literal("Zresetowano twój postęp."), false);
                    return 1;
                }))
                .then(Commands.literal("odbuduj_lobby").executes(c -> {
                    GameManager.command("kill @e[tag=rc_lobby]");
                    GameManager.buildLobby();
                    c.getSource().sendSuccess(() -> Component.literal("Lobby odbudowane."), true);
                    return 1;
                })));
    }
}
