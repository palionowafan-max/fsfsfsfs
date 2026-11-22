package com.robloxcloudly.game;

import com.robloxcloudly.Defs;
import com.robloxcloudly.model.Displays;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Układ mapy Steal an Egg (wymiar robloxcloudly:steal_egg).
 * Mapa nr i leży na x = i*512. Hub z 5 farmami: z od -23 do 44. Światy ciągną się na północ (z ujemne), każdy 64 bloki.
 */
public final class Layout {
    private Layout() {}

    public static final int SPACING = 512;
    public static final int WL = 64;      // długość świata
    public static final int PEDS = 6;     // jajek na świat
    public static final int[] FARM_DX = {-64, -32, 0, 32, 64};
    public static final String DIM = "robloxcloudly:steal_egg";

    public static int ox(int idx) { return idx * SPACING; }
    public static int fx(int idx, int farm) { return ox(idx) + FARM_DX[farm]; }
    public static int z0(int w) { return -24 - w * WL; }
    public static int z1(int w) { return z0(w) - WL; }

    public static int worldAt(double z) {
        if (z > -24) return -1;
        return Math.min(Defs.WORLDS.size() - 1, (int) Math.floor((-24 - z) / WL));
    }

    public static boolean inPlot(double x, double z, int fx) { return x >= fx - 12 && x < fx + 13 && z >= -4 && z < 21; }
    public static boolean onTreadmillArea(double x, double z, int fx) { return x >= fx - 1 && x < fx + 2 && z >= 23 && z < 29; }
    public static boolean onPad(double x, double z, int fx) { return x >= fx + 5 && x < fx + 8 && z >= 24 && z < 27; }
    public static int slotX(int fx, int i) { return fx - 8 + (i % 5) * 4; }
    public static int slotZ(int i) { return -1 + (i / 5) * 4; }

    /** Pozycje piedestałów: [świat][nr] = {dx od środka mapy, z} */
    public static final int[][][] PED = new int[9][PEDS][2];

    static {
        for (int w = 0; w < 9; w++) {
            Random r = new Random(1000 + w * 31L);
            int n = 0, tries = 0;
            while (n < PEDS && tries < 5000) {
                tries++;
                int dx = r.nextInt(37) - 18;
                int z = z1(w) + 4 + r.nextInt(27);
                boolean ok = true;
                for (int k = 0; k < n; k++) {
                    int ax = PED[w][k][0] - dx, az = PED[w][k][1] - z;
                    if (ax * ax + az * az < 49) { ok = false; break; }
                }
                if (ok) { PED[w][n][0] = dx; PED[w][n][1] = z; n++; }
            }
        }
    }

    private static BlockState b(Block x) { return x.defaultBlockState(); }
    private static BlockState leaves(Block x) { return x.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true); }

    public static String textDisplay(String dim, double x, double y, double z, String tag, String text, String color, boolean bold, double scale) {
        return String.format(Locale.ROOT,
                "execute in %s run summon minecraft:text_display %.2f %.2f %.2f {Tags:[\"rc\",\"rc_static\",\"%s\"],billboard:\"center\",shadow:1b,background:1073741824,text:%s,transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[%.2ff,%.2ff,%.2ff]}}",
                dim, x, y, z, tag, Displays.text(text, color, bold), scale, scale, scale);
    }

    private record Theme(BlockState top, BlockState base, BlockState wall) {}

    private static Theme theme(int w) {
        return switch (w) {
            case 0 -> new Theme(b(Blocks.GRASS_BLOCK), b(Blocks.DIRT), b(Blocks.MOSSY_STONE_BRICKS));
            case 1 -> new Theme(b(Blocks.GRASS_BLOCK), b(Blocks.DIRT), b(Blocks.STONE_BRICKS));
            case 2 -> new Theme(b(Blocks.SAND), b(Blocks.SANDSTONE), b(Blocks.SMOOTH_SANDSTONE));
            case 3 -> new Theme(b(Blocks.MOSS_BLOCK), b(Blocks.DIRT), b(Blocks.MOSSY_COBBLESTONE));
            case 4 -> new Theme(b(Blocks.SNOW_BLOCK), b(Blocks.DIRT), b(Blocks.PACKED_ICE));
            case 5 -> new Theme(b(Blocks.BLACKSTONE), b(Blocks.BASALT), b(Blocks.POLISHED_BLACKSTONE_BRICKS));
            case 6 -> new Theme(b(Blocks.DARK_PRISMARINE), b(Blocks.PRISMARINE), b(Blocks.PRISMARINE_BRICKS));
            case 7 -> new Theme(b(Blocks.COARSE_DIRT), b(Blocks.DIRT), b(Blocks.MUD_BRICKS));
            default -> new Theme(b(Blocks.END_STONE), b(Blocks.OBSIDIAN), b(Blocks.PURPUR_BLOCK));
        };
    }

    public static void buildMap(BuildQueue q, int idx) {
        int ox = ox(idx);
        String tag = "rc_map_" + idx;
        Random rnd = new Random(idx * 7919L + 17);
        BlockState air = b(Blocks.AIR), dirt = b(Blocks.DIRT), grass = b(Blocks.GRASS_BLOCK), barrier = b(Blocks.BARRIER);
        BlockState fence = b(Blocks.OAK_FENCE), path = b(Blocks.DIRT_PATH), log = b(Blocks.OAK_LOG), planks = b(Blocks.OAK_PLANKS);

        // ===== HUB =====
        q.fill(ox - 84, 63, -23, ox + 84, 63, 44, dirt);
        q.fill(ox - 84, 64, -23, ox + 84, 64, 44, grass);
        q.fill(ox - 85, 64, -24, ox - 85, 82, 45, barrier);
        q.fill(ox + 85, 64, -24, ox + 85, 82, 45, barrier);
        q.fill(ox - 85, 64, 45, ox + 85, 82, 45, barrier);
        q.fill(ox - 85, 64, -24, ox - 25, 82, -24, barrier);
        q.fill(ox + 25, 64, -24, ox + 85, 82, -24, barrier);
        q.fill(ox - 84, 65, -23, ox - 84, 65, 44, fence);
        q.fill(ox + 84, 65, -23, ox + 84, 65, 44, fence);
        q.fill(ox - 84, 65, 44, ox + 84, 65, 44, fence);
        q.fill(ox - 84, 65, -23, ox - 25, 65, -23, fence);
        q.fill(ox + 25, 65, -23, ox + 84, 65, -23, fence);
        q.fill(ox - 80, 64, -10, ox + 80, 64, -8, path);
        q.fill(ox - 3, 64, -23, ox + 3, 64, -8, path);
        q.fill(ox - 80, 64, 31, ox + 80, 64, 35, path);
        // brama do światów
        BlockState quartz = b(Blocks.QUARTZ_BLOCK);
        q.fill(ox - 7, 65, -21, ox - 6, 72, -21, quartz);
        q.fill(ox + 6, 65, -21, ox + 7, 72, -21, quartz);
        q.fill(ox - 7, 73, -21, ox + 7, 74, -21, quartz);
        q.set(ox - 6, 72, -20, b(Blocks.SEA_LANTERN));
        q.set(ox + 6, 72, -20, b(Blocks.SEA_LANTERN));

        // ===== FARMY =====
        BlockState tread = b(Blocks.POLISHED_BLACKSTONE);
        for (int f = 0; f < 5; f++) {
            int fx = ox + FARM_DX[f];
            q.fill(fx - 12, 64, -4, fx + 12, 64, 20, planks);
            q.fill(fx - 11, 64, -3, fx + 11, 64, 19, b(Blocks.MOSS_BLOCK));
            q.fill(fx - 12, 65, -4, fx + 12, 65, -4, fence);
            q.fill(fx - 12, 65, 20, fx + 12, 65, 20, fence);
            q.fill(fx - 12, 65, -4, fx - 12, 65, 20, fence);
            q.fill(fx + 12, 65, -4, fx + 12, 65, 20, fence);
            q.fill(fx - 2, 65, -4, fx + 2, 65, -4, air);
            q.fill(fx - 2, 65, 20, fx + 2, 65, 20, air);
            for (int zz : new int[]{-4, 20}) {
                q.fill(fx - 3, 65, zz, fx - 3, 68, zz, log);
                q.fill(fx + 3, 65, zz, fx + 3, 68, zz, log);
                q.fill(fx - 3, 69, zz, fx + 3, 69, zz, planks);
            }
            for (int i = 0; i < PlayerData.MAX_PETS; i++) q.set(slotX(fx, i), 64, slotZ(i), b(Blocks.SMOOTH_STONE));
            q.fill(fx - 2, 64, 22, fx + 2, 64, 29, b(Blocks.YELLOW_CONCRETE));
            q.fill(fx - 1, 64, 23, fx + 1, 64, 28, tread);
            q.fill(fx - 2, 65, 23, fx - 2, 65, 28, b(Blocks.IRON_BARS));
            q.fill(fx + 2, 65, 23, fx + 2, 65, 28, b(Blocks.IRON_BARS));
            q.fill(fx - 2, 65, 22, fx + 2, 66, 22, b(Blocks.IRON_BARS));
            q.fill(fx + 5, 64, 24, fx + 7, 64, 26, b(Blocks.GOLD_BLOCK));
            q.set(fx + 6, 65, 23, b(Blocks.LANTERN));
            if (f < 4) {
                tree(q, fx + 16, 2, log, leaves(Blocks.OAK_LEAVES), 5, 2);
                tree(q, fx + 16, 14, log, leaves(Blocks.BIRCH_LEAVES), 5, 2);
            }
        }
        for (int k = 0; k < 10; k++) {
            int side = k % 2 == 0 ? -1 : 1;
            tree(q, ox + side * (79 + rnd.nextInt(3)), -18 + k * 6, log, leaves(Blocks.OAK_LEAVES), 4 + rnd.nextInt(3), 2);
        }

        // ===== ŚWIATY =====
        for (int w = 0; w < Defs.WORLDS.size(); w++) {
            Defs.WorldDef wd = Defs.WORLDS.get(w);
            Theme th = theme(w);
            int z0 = z0(w), z1 = z1(w);
            q.fill(ox - 23, 63, z1 + 1, ox + 23, 63, z0, th.base());
            q.fill(ox - 23, 64, z1 + 1, ox + 23, 64, z0, th.top());
            q.fill(ox - 24, 64, z1 + 1, ox - 24, 70, z0, th.wall());
            q.fill(ox + 24, 64, z1 + 1, ox + 24, 70, z0, th.wall());
            q.fill(ox - 24, 71, z1 + 1, ox - 24, 82, z0, barrier);
            q.fill(ox + 24, 71, z1 + 1, ox + 24, 82, z0, barrier);
            q.fill(ox - 8, 65, z0, ox - 7, 72, z0, th.wall());
            q.fill(ox + 7, 65, z0, ox + 8, 72, z0, th.wall());
            q.fill(ox - 8, 73, z0, ox + 8, 74, z0, th.wall());
            q.set(ox - 6, 72, z0, b(Blocks.SEA_LANTERN));
            q.set(ox + 6, 72, z0, b(Blocks.SEA_LANTERN));
            List<int[]> taken = new ArrayList<>();
            for (int p = 0; p < PEDS; p++) {
                int px = ox + PED[w][p][0], pz = PED[w][p][1];
                q.set(px, 65, pz, b(Blocks.QUARTZ_PILLAR));
                q.set(px, 64, pz, b(Blocks.GOLD_BLOCK));
                taken.add(new int[]{px, pz});
            }
            taken.add(new int[]{ox, z1 + 18});
            decorate(q, w, ox, z0, z1, rnd, taken);
        }
        int zEnd = z1(Defs.WORLDS.size() - 1);
        q.fill(ox - 24, 63, zEnd, ox + 24, 82, zEnd, theme(8).wall());

        // ===== NAPISY =====
        q.cmd(textDisplay(DIM, ox + 0.5, 78.0, -20.5, tag, "robloxcloudly.gg", "aqua", true, 3.0));
        q.cmd(textDisplay(DIM, ox + 0.5, 76.4, -20.5, tag, "★ STEAL AN EGG ★", "gold", true, 2.0));
        q.cmd(textDisplay(DIM, ox + 0.5, 69.5, -20.5, tag, "⬆ ŚWIATY ⬆", "yellow", true, 1.5));
        for (int f = 0; f < 5; f++) {
            int fx = ox + FARM_DX[f];
            q.cmd(textDisplay(DIM, fx + 0.5, 67.8, 22.5, tag, "BIEŻNIA – trenuj szybkość", "yellow", true, 0.9));
            q.cmd(textDisplay(DIM, fx + 6.5, 66.3, 25.5, tag, "Stań tu – zbierz monety", "green", true, 0.8));
        }
        for (int w = 0; w < Defs.WORLDS.size(); w++) {
            Defs.WorldDef wd = Defs.WORLDS.get(w);
            int z0 = z0(w);
            q.cmd(textDisplay(DIM, ox + 0.5, 79.2, z0 + 0.5, tag, wd.name().toUpperCase(Locale.ROOT), "white", true, 3.0));
            q.cmd(textDisplay(DIM, ox + 0.5, 77.6, z0 + 0.5, tag, wd.req() > 0 ? "Wymagana szybkość: ⚡ " + wd.req() : "Świat startowy", "yellow", true, 1.5));
            q.cmd(textDisplay(DIM, ox + 0.5, 76.6, z0 + 0.5, tag, "Boss: " + wd.boss() + " (⚡ " + wd.bossSpeed() + ")", "red", true, 1.3));
        }
    }

    // ---------------- dekoracje ----------------
    static void tree(BuildQueue q, int x, int z, BlockState log, BlockState leaf, int h, int r) {
        for (int y = 65; y < 65 + h; y++) q.set(x, y, z, log);
        int cy = 64 + h;
        for (int dx = -r; dx <= r; dx++)
            for (int dy = -1; dy <= r; dy++)
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy * 2 + dz * dz > r * r + 1) continue;
                    if (dx == 0 && dz == 0 && dy <= 0) continue;
                    q.set(x + dx, cy + dy, z + dz, leaf);
                }
    }

    static void spruce(BuildQueue q, int x, int z, int h, boolean snow) {
        BlockState log = b(Blocks.SPRUCE_LOG), leaf = leaves(Blocks.SPRUCE_LEAVES);
        for (int y = 65; y < 65 + h; y++) q.set(x, y, z, log);
        for (int y = 67; y < 65 + h; y++) {
            int r = (y % 2 == 0) ? 1 : 2;
            if (y > 62 + h) r = 1;
            for (int dx = -r; dx <= r; dx++)
                for (int dz = -r; dz <= r; dz++) {
                    if ((dx == 0 && dz == 0) || (Math.abs(dx) == 2 && Math.abs(dz) == 2)) continue;
                    q.set(x + dx, y, z + dz, leaf);
                }
        }
        q.set(x, 65 + h, z, leaf);
        if (snow) q.set(x, 66 + h, z, b(Blocks.SNOW));
    }

    static void palm(BuildQueue q, int x, int z, int h) {
        BlockState log = b(Blocks.JUNGLE_LOG), leaf = leaves(Blocks.JUNGLE_LEAVES);
        for (int y = 65; y < 65 + h; y++) q.set(x, y, z, log);
        int ty = 64 + h;
        q.set(x, ty + 1, z, leaf);
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            for (int k = 1; k <= 3; k++) q.set(x + d[0] * k, ty + (k == 3 ? -1 : 0), z + d[1] * k, leaf);
        }
    }

    static void disc(BuildQueue q, int x, int y, int z, int r, BlockState st) {
        for (int dx = -r; dx <= r; dx++)
            for (int dz = -r; dz <= r; dz++)
                if (dx * dx + dz * dz <= r * r) q.set(x + dx, y, z + dz, st);
    }

    private static int[] spot(Random rnd, int ox, int z0, int z1, List<int[]> taken, int minDist) {
        for (int t = 0; t < 60; t++) {
            int dx = rnd.nextInt(43) - 21;
            if (Math.abs(dx) < 4) continue;
            int x = ox + dx, z = z1 + 2 + rnd.nextInt(Math.max(1, z0 - z1 - 5));
            boolean ok = true;
            for (int[] p : taken) {
                int ax = p[0] - x, az = p[1] - z;
                if (ax * ax + az * az < minDist * minDist) { ok = false; break; }
            }
            if (ok) { taken.add(new int[]{x, z}); return new int[]{x, z}; }
        }
        return null;
    }

    static void decorate(BuildQueue q, int w, int ox, int z0, int z1, Random rnd, List<int[]> taken) {
        switch (w) {
            case 0 -> { // Las
                BlockState[] flowers = {b(Blocks.POPPY), b(Blocks.DANDELION), b(Blocks.CORNFLOWER), b(Blocks.ALLIUM), b(Blocks.AZURE_BLUET), b(Blocks.OXEYE_DAISY)};
                for (int i = 0; i < 16; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 5); if (s != null) tree(q, s[0], s[1], b(Blocks.OAK_LOG), leaves(Blocks.OAK_LEAVES), 4 + rnd.nextInt(3), 2); }
                for (int i = 0; i < 40; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 65, s[1], flowers[rnd.nextInt(flowers.length)]); }
                for (int i = 0; i < 10; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) { q.set(s[0], 65, s[1], leaves(Blocks.OAK_LEAVES)); q.set(s[0] + 1, 65, s[1], leaves(Blocks.OAK_LEAVES)); } }
            }
            case 1 -> { // Jezioro
                for (int i = 0; i < 6; i++) {
                    int[] s = spot(rnd, ox, z0, z1, taken, 8);
                    if (s == null) continue;
                    int r = 2 + rnd.nextInt(2);
                    int cx = Math.max(ox - 18, Math.min(ox + 18, s[0]));
                    disc(q, cx, 64, s[1], r, b(Blocks.WATER));
                    for (int k = 0; k < 4; k++) q.set(cx + rnd.nextInt(r * 2 - 1) - (r - 1), 65, s[1] + rnd.nextInt(r * 2 - 1) - (r - 1), b(Blocks.LILY_PAD));
                }
                for (int i = 0; i < 10; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 5); if (s != null) tree(q, s[0], s[1], b(Blocks.BIRCH_LOG), leaves(Blocks.BIRCH_LEAVES), 5, 2); }
                for (int i = 0; i < 30; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 65, s[1], b(Blocks.SHORT_GRASS)); }
            }
            case 2 -> { // Pustynia
                for (int side = -1; side <= 1; side += 2) {
                    int px = ox + side * 18, pz = (z0 + z1) / 2 + side * 14;
                    for (int l = 0; l < 4; l++) q.fill(px - 3 + l, 65 + l, pz - 3 + l, px + 3 - l, 65 + l, pz + 3 - l, b(Blocks.SMOOTH_SANDSTONE));
                    taken.add(new int[]{px, pz});
                }
                for (int i = 0; i < 16; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) { int h = 2 + rnd.nextInt(2); for (int y = 0; y < h; y++) q.set(s[0], 65 + y, s[1], b(Blocks.CACTUS)); } }
                for (int i = 0; i < 20; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 65, s[1], b(Blocks.DEAD_BUSH)); }
                for (int i = 0; i < 8; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) q.set(s[0], 65, s[1], b(Blocks.TERRACOTTA)); }
            }
            case 3 -> { // Dżungla
                for (int i = 0; i < 11; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 7); if (s != null) tree(q, s[0], s[1], b(Blocks.JUNGLE_LOG), leaves(Blocks.JUNGLE_LEAVES), 8 + rnd.nextInt(3), 3); }
                for (int i = 0; i < 40; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 65, s[1], b(Blocks.FERN)); }
                for (int i = 0; i < 12; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) { q.set(s[0], 65, s[1], leaves(Blocks.JUNGLE_LEAVES)); q.set(s[0], 66, s[1], leaves(Blocks.JUNGLE_LEAVES)); } }
                for (int i = 0; i < 6; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) q.set(s[0], 65, s[1], b(Blocks.MELON)); }
            }
            case 4 -> { // Śnieg
                for (int i = 0; i < 16; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 5); if (s != null) spruce(q, s[0], s[1], 6 + rnd.nextInt(3), true); }
                for (int i = 0; i < 10; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) { int h = 3 + rnd.nextInt(5); for (int y = 0; y < h; y++) q.set(s[0], 65 + y, s[1], b(Blocks.PACKED_ICE)); } }
                for (int i = 0; i < 30; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 65, s[1], b(Blocks.SNOW)); }
                for (int i = 0; i < 8; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) q.set(s[0], 64, s[1], b(Blocks.BLUE_ICE)); }
            }
            case 5 -> { // Wulkan
                for (int i = 0; i < 6; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 7); if (s != null) disc(q, Math.max(ox - 18, Math.min(ox + 18, s[0])), 64, s[1], 2, b(Blocks.LAVA)); }
                for (int i = 0; i < 40; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 64, s[1], b(Blocks.MAGMA_BLOCK)); }
                for (int i = 0; i < 12; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) { int h = 2 + rnd.nextInt(5); for (int y = 0; y < h; y++) q.set(s[0], 65 + y, s[1], b(Blocks.BASALT)); } }
                for (int side = -1; side <= 1; side += 2) {
                    int px = ox + side * 17, pz = (z0 + z1) / 2 - side * 12;
                    for (int l = 0; l < 5; l++) q.fill(px - 4 + l, 65 + l, pz - 4 + l, px + 4 - l, 65 + l, pz + 4 - l, b(Blocks.BLACKSTONE));
                    q.set(px, 69, pz, b(Blocks.LAVA));
                    taken.add(new int[]{px, pz});
                }
            }
            case 6 -> { // Otchłań Oceanu
                BlockState[] coral = {b(Blocks.PINK_CONCRETE), b(Blocks.ORANGE_CONCRETE), b(Blocks.PURPLE_CONCRETE), b(Blocks.LIME_CONCRETE), b(Blocks.MAGENTA_CONCRETE)};
                for (int i = 0; i < 30; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 64, s[1], b(Blocks.SEA_LANTERN)); }
                for (int i = 0; i < 20; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) { BlockState c = coral[rnd.nextInt(coral.length)]; int h = 2 + rnd.nextInt(4); for (int y = 0; y < h; y++) q.set(s[0], 65 + y, s[1], c); q.set(s[0] + 1, 66, s[1], c); } }
                for (int i = 0; i < 40; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 68 + rnd.nextInt(8), s[1], b(Blocks.LIGHT_BLUE_STAINED_GLASS)); }
            }
            case 7 -> { // Prehistoria
                for (int i = 0; i < 20; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 3); if (s != null) disc(q, s[0], 64, s[1], 1, b(Blocks.MOSS_BLOCK)); }
                for (int i = 0; i < 10; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 5); if (s != null) palm(q, s[0], s[1], 6 + rnd.nextInt(3)); }
                for (int i = 0; i < 30; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 65, s[1], b(Blocks.FERN)); }
                for (int i = 0; i < 6; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 4); if (s != null) { q.fill(s[0] - 2, 65, s[1], s[0] + 2, 65, s[1], b(Blocks.BONE_BLOCK)); q.set(s[0] + 2, 66, s[1], b(Blocks.BONE_BLOCK)); } }
                for (int i = 0; i < 4; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 4); if (s != null) { disc(q, s[0], 65, s[1], 1, b(Blocks.HAY_BLOCK)); q.set(s[0], 66, s[1], b(Blocks.WHITE_CONCRETE)); } }
            }
            default -> { // Kosmos
                for (int i = 0; i < 12; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 4); if (s != null) { int h = 3 + rnd.nextInt(6); for (int y = 0; y < h; y++) q.set(s[0], 65 + y, s[1], b(Blocks.PURPUR_PILLAR)); q.set(s[0], 65 + h, s[1], b(Blocks.END_ROD)); } }
                for (int i = 0; i < 20; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) { q.set(s[0], 65, s[1], b(Blocks.AMETHYST_BLOCK)); q.set(s[0], 66, s[1], b(Blocks.AMETHYST_CLUSTER)); } }
                for (int i = 0; i < 20; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 64, s[1], b(Blocks.CRYING_OBSIDIAN)); }
                for (int i = 0; i < 16; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 2); if (s != null) q.set(s[0], 65, s[1], b(Blocks.END_ROD)); }
                for (int i = 0; i < 8; i++) { int[] s = spot(rnd, ox, z0, z1, taken, 4); if (s != null) disc(q, s[0], 74 + rnd.nextInt(6), s[1], 1, b(Blocks.END_STONE)); }
            }
        }
    }
}
