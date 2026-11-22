package com.robloxcloudly.game;

import com.robloxcloudly.model.Displays;
import com.robloxcloudly.model.PetModels;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/** Lobby (wymiar robloxcloudly:lobby): latająca wyspa, spawn, napis robloxcloudly.gg, podium ze skinem, hoverboard z boku, chmury. */
public final class Lobby {
    private Lobby() {}

    public static final String NPC_NAME = "Ziomsigiemka_1";   // zmień nick skina tutaj
    public static final String DIM = "robloxcloudly:lobby";
    public static final double SPAWN_X = 0.5, SPAWN_Y = 101, SPAWN_Z = 16.5;
    public static final double NPC_X = 0.5, NPC_Y = 102, NPC_Z = -7.5;
    public static final double BOARD_X = 15.5, BOARD_Y = 102.8, BOARD_Z = 0.5;

    private static BlockState b(Block x) { return x.defaultBlockState(); }

    public static void build(BuildQueue q) {
        String tag = "rc_lobby";
        Random rnd = new Random(2026);
        BlockState grass = b(Blocks.GRASS_BLOCK), dirt = b(Blocks.DIRT), stone = b(Blocks.STONE);
        // wyspa
        for (int x = -30; x <= 30; x++)
            for (int z = -30; z <= 30; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > 28) continue;
                q.set(x, 100, z, grass);
                int depth = (int) ((28 - d) * 0.55) + 2;
                for (int y = 1; y <= depth; y++) q.set(x, 100 - y, z, y < 3 ? dirt : stone);
            }
        // spawn
        for (int x = -3; x <= 3; x++)
            for (int z = 13; z <= 19; z++)
                q.set(x, 100, z, ((x + z) & 1) == 0 ? b(Blocks.QUARTZ_BLOCK) : b(Blocks.CHISELED_QUARTZ_BLOCK));
        q.fill(-1, 100, -5, 1, 100, 12, b(Blocks.DIRT_PATH));
        for (int z : new int[]{-3, 3, 9}) { q.set(-3, 101, z, b(Blocks.LANTERN)); q.set(3, 101, z, b(Blocks.LANTERN)); }
        // podium ze skinem
        q.fill(-2, 101, -9, 2, 101, -6, b(Blocks.QUARTZ_BLOCK));
        q.fill(-2, 100, -9, 2, 100, -6, b(Blocks.GOLD_BLOCK));
        // portal (ring) za skinem
        for (int x = -6; x <= 6; x++)
            for (int y = -6; y <= 6; y++) {
                double d = Math.sqrt(x * x + y * y);
                if (d >= 4.5 && d <= 5.9) q.set(x, 106 + y, -12, ((x + y) & 1) == 0 ? b(Blocks.PURPLE_CONCRETE) : b(Blocks.CRYING_OBSIDIAN));
                else if (d < 4.5) q.set(x, 106 + y, -12, b(Blocks.PURPLE_STAINED_GLASS));
            }
        // hoverboard z boku
        q.fill(14, 101, -1, 16, 101, 1, b(Blocks.LIGHT_BLUE_CONCRETE));
        q.set(15, 101, 0, b(Blocks.SEA_LANTERN));
        // zablokowany tryb
        q.fill(-16, 101, -1, -14, 101, 1, b(Blocks.GRAY_CONCRETE));
        // drzewa
        for (int k = 0; k < 10; k++) {
            double a = k / 10.0 * Math.PI * 2 + 0.3;
            int x = (int) Math.round(Math.cos(a) * 22), z = (int) Math.round(Math.sin(a) * 22);
            Block leaf = k % 2 == 0 ? Blocks.OAK_LEAVES : Blocks.BIRCH_LEAVES;
            Block log = k % 2 == 0 ? Blocks.OAK_LOG : Blocks.BIRCH_LOG;
            int h = 4 + rnd.nextInt(2);
            for (int y = 101; y < 101 + h; y++) q.set(x, y, z, b(log));
            for (int dx = -2; dx <= 2; dx++)
                for (int dy = -1; dy <= 2; dy++)
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx * dx + dy * dy * 2 + dz * dz > 5) continue;
                        if (dx == 0 && dz == 0 && dy <= 0) continue;
                        q.set(x + dx, 100 + h + dy, z + dz, leaf.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
                    }
        }
        // chmury (cloudly!)
        for (int c = 0; c < 26; c++) {
            double a = rnd.nextDouble() * Math.PI * 2, r = 38 + rnd.nextDouble() * 34;
            int cx = (int) (Math.cos(a) * r), cz = (int) (Math.sin(a) * r), cy = 88 + rnd.nextInt(34);
            int rx = 3 + rnd.nextInt(3), rz = 2 + rnd.nextInt(2);
            for (int dx = -rx; dx <= rx; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -rz; dz <= rz; dz++) {
                        double e = (dx * dx) / (double) (rx * rx) + (dy * dy) / 1.5 + (dz * dz) / (double) (rz * rz);
                        if (e <= 1.0) q.set(cx + dx, cy + dy, cz + dz, b(Blocks.WHITE_WOOL));
                    }
        }
        // skin (manekin z prawdziwym skinem gracza o tym nicku) + strefy kliknięcia
        String lt = Displays.tags("rc", "rc_lobby");
        q.cmd(String.format(java.util.Locale.ROOT, "execute in %s run summon minecraft:mannequin %.2f %.2f %.2f {%s,Rotation:[0f,0f],Invulnerable:1b,NoGravity:1b,immovable:1b,profile:{name:\"%s\"},CustomName:%s,CustomNameVisible:1b}",
                DIM, NPC_X, NPC_Y, NPC_Z, lt, NPC_NAME, Displays.text(NPC_NAME, "white", true)));
        q.cmd(String.format(java.util.Locale.ROOT, "execute in %s run summon minecraft:interaction %.2f %.2f %.2f {%s,width:1.4f,height:2.2f,response:1b}", DIM, NPC_X, NPC_Y, NPC_Z, lt));
        q.cmd(Displays.summonModel(DIM, BOARD_X, BOARD_Y, BOARD_Z, 35.0F, PetModels.board(), 0.6, lt, null, 1.0, 1.0, 0));
        q.cmd(String.format(java.util.Locale.ROOT, "execute in %s run summon minecraft:interaction %.2f %.2f %.2f {%s,width:2.2f,height:1.2f,response:1b}", DIM, BOARD_X, BOARD_Y - 0.6, BOARD_Z, lt));
        // napisy
        q.cmd(Layout.textDisplay(DIM, 0.5, 117.0, -13.5, tag, "robloxcloudly.gg", "aqua", true, 6.0));
        q.cmd(Layout.textDisplay(DIM, 0.5, 114.6, -13.5, tag, "☁ Witaj na serwerze! ☁", "yellow", true, 2.5));
        q.cmd(Layout.textDisplay(DIM, NPC_X, 105.7, NPC_Z, tag, "★ STEAL AN EGG ★", "gold", true, 1.8));
        q.cmd(Layout.textDisplay(DIM, NPC_X, 105.0, NPC_Z, tag, "Kliknij PPM, aby grać!", "green", true, 1.0));
        q.cmd(Layout.textDisplay(DIM, BOARD_X, 105.2, BOARD_Z, tag, "HOVERBOARD", "aqua", true, 1.6));
        q.cmd(Layout.textDisplay(DIM, BOARD_X, 104.6, BOARD_Z, tag, "Kliknij deskę, aby ją odebrać", "white", false, 0.9));
        q.cmd(Layout.textDisplay(DIM, -14.5, 104.5, 0.5, tag, "WKRÓTCE: nowy tryb", "gray", true, 1.4));
        q.cmd(Layout.textDisplay(DIM, SPAWN_X, 103.5, SPAWN_Z + 2, tag, "SPAWN", "white", true, 1.2));
    }
}
