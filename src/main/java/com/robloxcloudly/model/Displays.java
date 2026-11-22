package com.robloxcloudly.model;

import java.util.List;
import java.util.Locale;

/** Zamienia model z klocków na komendę /summon z block_display (działa w zwykłym Minecrafcie, bez moda u gracza). */
public final class Displays {
    private Displays() {}

    // paleta bloków: nazwa + kolor
    private static final Object[][] PALETTE = {
            {"white_concrete", 0xCFD5D6}, {"orange_concrete", 0xE06100}, {"magenta_concrete", 0xA9309F}, {"light_blue_concrete", 0x2389C6},
            {"yellow_concrete", 0xF0AF15}, {"lime_concrete", 0x5EA818}, {"pink_concrete", 0xD5658E}, {"gray_concrete", 0x36393D},
            {"light_gray_concrete", 0x7D7D73}, {"cyan_concrete", 0x157788}, {"purple_concrete", 0x641F9C}, {"blue_concrete", 0x2C2E8F},
            {"brown_concrete", 0x603B1F}, {"green_concrete", 0x495B24}, {"red_concrete", 0x8E2020}, {"black_concrete", 0x080A0F},
            {"white_wool", 0xE9ECEC}, {"orange_wool", 0xF07613}, {"magenta_wool", 0xBD44B3}, {"light_blue_wool", 0x3AAFD9},
            {"yellow_wool", 0xF8C627}, {"lime_wool", 0x70B919}, {"pink_wool", 0xED8DAC}, {"gray_wool", 0x3E4447},
            {"light_gray_wool", 0x8E8E86}, {"cyan_wool", 0x158991}, {"purple_wool", 0x792AAC}, {"blue_wool", 0x35399D},
            {"brown_wool", 0x724728}, {"green_wool", 0x546D1B}, {"red_wool", 0xA12722}, {"black_wool", 0x141519},
            {"white_terracotta", 0xD1B1A1}, {"orange_terracotta", 0xA15325}, {"magenta_terracotta", 0x95586C}, {"light_blue_terracotta", 0x716C89},
            {"yellow_terracotta", 0xBA8523}, {"lime_terracotta", 0x677534}, {"pink_terracotta", 0xA14E4E}, {"gray_terracotta", 0x392A23},
            {"light_gray_terracotta", 0x876A61}, {"cyan_terracotta", 0x575B5B}, {"purple_terracotta", 0x764656}, {"blue_terracotta", 0x4A3B5B},
            {"brown_terracotta", 0x4D3323}, {"green_terracotta", 0x4C532A}, {"red_terracotta", 0x8F3D2E}, {"black_terracotta", 0x251610},
            {"terracotta", 0x985E43}, {"snow_block", 0xF9FEFE}, {"quartz_block", 0xEBE5DE}, {"gold_block", 0xF6D03D},
            {"diamond_block", 0x62EDE4}, {"emerald_block", 0x2ACB57}, {"lapis_block", 0x1F4597}, {"redstone_block", 0xAF1805},
            {"sand", 0xDBCFA3}, {"red_sand", 0xBE6621}, {"packed_ice", 0x8DB4FA}, {"bone_block", 0xE5E1CF},
            {"honeycomb_block", 0xE5941E}, {"moss_block", 0x596E2D}, {"oak_planks", 0xA2834F}, {"dark_oak_planks", 0x42291A},
            {"spruce_planks", 0x725430}, {"birch_planks", 0xC0AF79}, {"cherry_planks", 0xE3B3AD}, {"crimson_planks", 0x653147},
            {"warped_planks", 0x2B6963}, {"purpur_block", 0xA97DA9}, {"amethyst_block", 0x8662BF}, {"iron_block", 0xDCDCDC},
            {"obsidian", 0x0F0B19}, {"nether_wart_block", 0x732B2C}, {"warped_wart_block", 0x167879}, {"shroomlight", 0xF09246},
            {"prismarine_bricks", 0x63AC9E}, {"blue_ice", 0x74A8FD}, {"copper_block", 0xC06C50}, {"mud_bricks", 0x89684F},
    };

    public static String blockFor(int rgb) {
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        String best = "white_concrete";
        double bd = Double.MAX_VALUE;
        for (Object[] e : PALETTE) {
            int c = (Integer) e[1];
            int dr = r - ((c >> 16) & 255), dg = g - ((c >> 8) & 255), db = b - (c & 255);
            double d = 2.0 * dr * dr + 4.0 * dg * dg + 3.0 * db * db;
            if (d < bd) { bd = d; best = (String) e[0]; }
        }
        return best;
    }

    static String f(double v) { return String.format(Locale.ROOT, "%.4ff", v); }

    public static String tags(String... tags) {
        StringBuilder sb = new StringBuilder("Tags:[");
        for (int i = 0; i < tags.length; i++) {
            if (i > 0) sb.append(',');
            sb.append('"').append(tags[i]).append('"');
        }
        return sb.append(']').toString();
    }

    /** Jeden klocek -> block_display (pozycja względem pojazdu). k = rozmiar jednostki w blokach. */
    public static String piece(Shape.Piece p, double k, String tagStr) {
        double sx = Math.max(0.005, p.w * k), sy = Math.max(0.005, p.h * k), sz = Math.max(0.005, p.d * k);
        double cx = p.cx * k, cy = p.cy * k, cz = p.cz * k;
        // kwaternion z kątów Eulera XYZ: q = qx * qy * qz
        double hx = p.rx / 2.0, hy = p.ry / 2.0, hz = p.rz / 2.0;
        double[] qx = {Math.sin(hx), 0, 0, Math.cos(hx)};
        double[] qy = {0, Math.sin(hy), 0, Math.cos(hy)};
        double[] qz = {0, 0, Math.sin(hz), Math.cos(hz)};
        double[] q = mul(mul(qx, qy), qz);
        double[] half = rotate(q, sx / 2, sy / 2, sz / 2);
        double tx = cx - half[0], ty = cy - half[1], tz = cz - half[2];
        StringBuilder sb = new StringBuilder();
        sb.append("{id:\"minecraft:block_display\",").append(tagStr)
                .append(",block_state:{Name:\"minecraft:").append(blockFor(p.color)).append("\"}")
                .append(",transformation:{translation:[").append(f(tx)).append(',').append(f(ty)).append(',').append(f(tz))
                .append("],left_rotation:[").append(f(q[0])).append(',').append(f(q[1])).append(',').append(f(q[2])).append(',').append(f(q[3]))
                .append("],scale:[").append(f(sx)).append(',').append(f(sy)).append(',').append(f(sz))
                .append("],right_rotation:[0f,0f,0f,1f]}");
        if (p.glow) sb.append(",brightness:{sky:15,block:15}");
        sb.append('}');
        return sb.toString();
    }

    private static double[] mul(double[] a, double[] b) {
        return new double[]{
                a[3] * b[0] + a[0] * b[3] + a[1] * b[2] - a[2] * b[1],
                a[3] * b[1] - a[0] * b[2] + a[1] * b[3] + a[2] * b[0],
                a[3] * b[2] + a[0] * b[1] - a[1] * b[0] + a[2] * b[3],
                a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2]};
    }

    private static double[] rotate(double[] q, double x, double y, double z) {
        double[] p = {x, y, z, 0};
        double[] qc = {-q[0], -q[1], -q[2], q[3]};
        double[] r = mul(mul(q, p), qc);
        return new double[]{r[0], r[1], r[2]};
    }

    /** Tekst SNBT (komponent tekstowy). */
    public static String text(String text, String color, boolean bold) {
        String safe = text.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{text:\"" + safe + "\",color:\"" + color + "\",bold:" + (bold ? "1b" : "0b") + "}";
    }

    public static String text2(String a, String colorA, String b, String colorB) {
        String sa = a.replace("\\", "\\\\").replace("\"", "\\\""), sb = b.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{text:\"" + sa + "\",color:\"" + colorA + "\",bold:1b,extra:[{text:\"" + sb + "\",color:\"" + colorB + "\",bold:0b}]}";
    }

    /** text_display jako pasażer (lub samodzielny) na wysokości y. */
    public static String textEntity(String tagStr, String component, double y, double scale) {
        return "{id:\"minecraft:text_display\"," + tagStr + ",billboard:\"center\",shadow:1b,background:1073741824,text:" + component
                + ",transformation:{translation:[0f," + f(y) + ",0f],left_rotation:[0f,0f,0f,1f],scale:[" + f(scale) + "," + f(scale) + "," + f(scale) + "],right_rotation:[0f,0f,0f,1f]}}";
    }

    /** Pełna komenda: pusty block_display (korzeń) + klocki modelu jako pasażerowie + napis. */
    public static String summonModel(String dim, double x, double y, double z, float yaw, Shape.Model m, double k, String tagStr,
                                     String nameComponent, double nameScale, double viewRange, int teleportDuration) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "execute in %s run summon minecraft:block_display %.3f %.3f %.3f {", dim, x, y, z));
        sb.append(tagStr).append(",Rotation:[").append(f(yaw)).append(",0f]");
        sb.append(",view_range:").append(f(viewRange));
        if (teleportDuration > 0) sb.append(",teleport_duration:").append(teleportDuration);
        sb.append(",Passengers:[");
        List<Shape.Piece> ps = m.pieces();
        boolean first = true;
        for (Shape.Piece p : ps) {
            if (!first) sb.append(',');
            first = false;
            String one = piece(p, k, tagStr);
            // obrót pasażera = obrót modelu
            sb.append(one, 0, one.length() - 1).append(",Rotation:[").append(f(yaw)).append(",0f],view_range:").append(f(viewRange)).append('}');
        }
        if (nameComponent != null) {
            if (!first) sb.append(',');
            sb.append(textEntity(tagStr, nameComponent, m.top() * k + 0.35, nameScale));
        }
        sb.append("]}");
        return sb.toString();
    }
}
