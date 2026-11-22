package com.robloxcloudly.model;

import com.robloxcloudly.Defs;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static com.robloxcloudly.model.Shape.P;
import static com.robloxcloudly.model.Shape.Q;

/** Modele wszystkich petów (i bossów – te same modele z czerwonymi oczami). */
public final class PetModels {
    private PetModels() {}

    private static final Map<String, Shape.Model> CACHE = new HashMap<>();
    private static final Map<String, Shape.Model> EGGS = new HashMap<>();
    private static Shape.Model board;
    private static Shape.Model npc;

    public static Shape.Model get(String name, boolean boss) {
        return CACHE.computeIfAbsent(name + (boss ? "#boss" : ""), k -> build(name, boss));
    }

    private static Shape.Model build(String name, boolean angry) {
        Defs.PetDef d = Defs.get(name);
        Set<String> F = d.flags();
        Shape s = new Shape();
        s.c1 = d.c1();
        s.c2 = d.c2();
        s.glowMain = F.contains("glow") || F.contains("fire");
        s.angry = angry;
        Random rnd = new Random(name.hashCode());
        switch (d.type()) {
            case "bird" -> bird(s, F);
            case "fish" -> fish(s, F);
            case "squid" -> squid(s);
            case "serpent" -> serpent(s);
            case "frog" -> frog(s);
            case "turtle" -> turtle(s);
            case "lizard" -> lizard(s, F, rnd);
            case "ape" -> ape(s, F);
            case "blob" -> blob(s);
            case "snake" -> snake(s, F);
            case "spider" -> spider(s, F);
            case "dragon" -> dragon(s, F);
            case "dino" -> dino(s, F, rnd);
            case "pepper" -> pepper(s);
            case "worm" -> worm(s);
            default -> quad(s, F);
        }
        if (F.contains("fire")) {
            s.glow(0xff8800);
            for (int k = 0; k < 4; k++) s.K(0xff8800, (k % 2 == 0 ? -0.15 : 0.15), 2.0 + (k % 3) * 0.12, (k - 1.5) * 0.35, 0.12, 0.5);
        }
        return s.bake();
    }

    // ---------------- jajko ----------------
    public static Shape.Model egg(int world, int rarity) {
        return EGGS.computeIfAbsent(world + ":" + rarity, k -> {
            Shape s = new Shape();
            int col = Defs.RCOL[rarity];
            if (rarity >= 5) s.glow(col);
            s.B(col, 0, 0.2, 0, 1.05, 0.2, 1.05);
            s.B(col, 0, 0.75, 0, 1.45, 0.9, 1.45);
            s.B(col, 0, 1.5, 0, 1.3, 0.8, 1.3);
            s.B(col, 0, 2.05, 0, 0.95, 0.5, 0.95);
            s.B(col, 0, 2.35, 0, 0.55, 0.2, 0.55);
            int spot = rarity == 7 ? 0xffffff : Defs.WORLDS.get(world).spot();
            double[][] spots = {{0.73, 0.8, 0.2}, {-0.73, 1.1, -0.3}, {0.2, 1.6, 0.66}, {-0.3, 0.6, 0.73}, {0.66, 1.7, -0.2}, {-0.1, 1.2, -0.73}, {0.3, 2.1, 0.48}, {-0.48, 2.0, 0.1}};
            for (double[] p : spots) {
                double ax = Math.abs(p[0]), az = Math.abs(p[2]);
                if (ax > az) s.B(spot, p[0], p[1], p[2], 0.08, 0.3, 0.3);
                else s.B(spot, p[0], p[1], p[2], 0.3, 0.3, 0.08);
            }
            return s.bake();
        });
    }

    // ---------------- hoverboard ----------------
    public static Shape.Model board() {
        if (board == null) {
            Shape s = new Shape();
            s.glow(0x00e5ff);
            s.glow(0xff3df2);
            s.B(0x1e1e2e, 0, 0, 0, 1.5, 0.2, 3.6);
            s.B(0x00e5ff, 0, -0.12, 0, 1.2, 0.05, 3.2);
            s.B(0xff3df2, 0, 0.11, 0, 0.25, 0.02, 3.0);
            s.B(0x1e1e2e, 0, 0.08, 1.75, 1.2, 0.2, 0.3);
            s.B(0x1e1e2e, 0, 0.08, -1.75, 1.2, 0.2, 0.3);
            for (int z = -1; z <= 1; z += 2) for (int x = -1; x <= 1; x += 2) s.C(0x00e5ff, x * 0.5, -0.18, z * 1.3, 0.2, 0.12);
            board = s.bake();
        }
        return board;
    }

    // ================= typy ciał =================
    private static void qHead(Shape s, Set<String> F) {
        s.B(P, 0, 0, 0, 0.9, 0.8, 0.8);
        s.B(Q, 0, -0.15, 0.5, 0.46, 0.36, 0.34);
        s.S(0x111111, 0, -0.03, 0.68, 0.08, 0.07, 0.05);
        if (F.contains("cow")) {
            s.B(0xffb3c6, 0, -0.18, 0.62, 0.5, 0.3, 0.12);
            for (int k = -1; k <= 1; k += 2) s.K(0xf5f0d0, k * 0.38, 0.5, 0, 0.07, 0.35).rz(-k * 0.6);
        }
        if (F.contains("mask")) s.B(0x222222, 0, 0.12, 0.402, 0.92, 0.24, 0.02);
        s.eyes(0, 0.13, 0.41, 0.22, 0.1);
        for (int k = -1; k <= 1; k += 2) {
            if (F.contains("floppy")) s.B(Q, k * 0.5, -0.05, 0, 0.12, 0.55, 0.32);
            else if (F.contains("trunk")) s.B(P, k * 0.58, 0, -0.1, 0.12, 0.8, 0.7);
            else if (F.contains("round")) s.S(P, k * 0.34, 0.45, -0.05, 0.18, 0.18, 0.1);
            else if (F.contains("hugeears")) s.K(P, k * 0.38, 0.75, 0, 0.3, 1.0, 0.12).rz(-k * 0.25);
            else if (F.contains("bigears")) s.K(P, k * 0.28, 0.7, 0, 0.18, 0.8, 0.1).rz(-k * 0.15);
            else if (!F.contains("sphinx") && !F.contains("tusks") && !F.contains("cow")) s.K(P, k * 0.28, 0.55, 0, 0.17, 0.4, 0.12);
            if (F.contains("tusks")) s.K(0xfff8e0, k * 0.22, -0.55, 0.45, 0.07, 0.75).rx(Math.PI - 0.35);
            if (F.contains("horns")) s.K(0xf5f0d0, k * 0.6, 0.35, 0, 0.1, 0.55).rz(-k * 1.1);
            if (F.contains("saber")) s.K(0xffffff, k * 0.13, -0.5, 0.55, 0.05, 0.45);
        }
        if (F.contains("trunk")) { s.C(P, 0, -0.4, 0.6, 0.14, 0.6).rx(0.3); s.C(P, 0, -0.85, 0.7, 0.11, 0.5); }
        if (F.contains("sphinx")) {
            s.B(Q, 0, 0.1, -0.25, 1.15, 1.05, 0.5);
            for (int k = 0; k < 3; k++) s.B(0xffd700, 0, -0.25 + k * 0.3, -0.24, 1.17, 0.08, 0.52);
            s.B(Q, 0, -0.55, 0.42, 0.15, 0.35, 0.12);
            s.crown(0, 0.5, 0, 0.3);
        }
        if (F.contains("crown")) s.crown(0, 0.45, 0, 0.33);
    }

    private static void quad(Shape s, Set<String> F) {
        double sc = F.contains("big") ? 1.3 : F.contains("small") ? 0.7 : 1.0, fat = F.contains("fat") ? 1.35 : 1.0;
        s.push(0, 0, 0, sc);
        s.B(P, 0, 1.1, 0, 1.3 * fat, 1.0 * fat, 2.0);
        if (F.contains("cow")) {
            double[][] sp = {{0.66, 1.2, 0.3}, {-0.66, 1.0, -0.4}, {0.66, 0.95, -0.6}, {-0.66, 1.3, 0.5}};
            for (double[] p : sp) s.B(Q, p[0], p[1], p[2], 0.05, 0.38, 0.45);
        }
        if (F.contains("stripes")) for (int k = -2; k <= 2; k++) s.B(Q, 0, 1.12, k * 0.38, 1.34 * fat, 1.03 * fat, 0.09);
        if (F.contains("hump")) { s.S(P, 0, 1.75, -0.35, 0.45, 0.45, 0.5); s.S(P, 0, 1.7, 0.4, 0.4, 0.4, 0.45); }
        for (int ix = -1; ix <= 1; ix += 2)
            for (int iz = -1; iz <= 1; iz += 2) {
                s.B(P, ix * 0.42 * fat, 0.35, iz * 0.7, 0.34, 0.7, 0.34);
                s.B(Q, ix * 0.42 * fat, 0.05, iz * 0.7 + 0.03, 0.36, 0.12, 0.4);
            }
        boolean lng = F.contains("long");
        double hy = lng ? 2.3 : 1.65, hz = lng ? 1.35 : 1.2;
        if (lng) s.C(P, 0, 1.85, 1.1, 0.22, 1.0).rx(0.5);
        double[] hxs = F.contains("threeheads") ? new double[]{-0.62, 0, 0.62} : new double[]{0};
        for (double hx : hxs) { s.push(hx, hy, hz, 1); qHead(s, F); s.pop(); }
        if (F.contains("bushy")) { s.K(P, 0, 1.5, -1.35, 0.28, 1.0).rx(-0.9); s.S(Q, 0, 1.83, -1.74, 0.17); }
        else if (F.contains("ringtail")) for (int k = 0; k < 4; k++) s.C(k % 2 == 1 ? Q : P, 0, 1.3 + k * 0.15, -1.1 - k * 0.22, 0.14, 0.25).rx(-0.9);
        else if (F.contains("longtail")) s.C(P, 0, 1.0, -1.6, 0.05, 1.4).rx(1.2);
        else s.C(P, 0, 1.3, -1.1, 0.08, 0.5).rx(-0.8);
        if (F.contains("ring")) {
            int g = 0xe8c06a;
            s.glow(g);
            s.B(g, 0, 1.2, 1.7, 3.4, 0.12, 0.12);
            s.B(g, 0, 1.2, -1.7, 3.4, 0.12, 0.12);
            s.B(g, 1.7, 1.2, 0, 0.12, 0.12, 3.4);
            s.B(g, -1.7, 1.2, 0, 0.12, 0.12, 3.4);
            s.S(0xe8b86a, 0.9, 2.4, -0.3, 0.32);
        }
        s.pop();
    }

    private static void bird(Shape s, Set<String> F) {
        double fat = F.contains("fat") ? 1.3 : 1.0;
        boolean peng = F.contains("penguin");
        double hy = 1.55, hz = 0.45;
        if (peng) { s.S(P, 0, 1.05, 0, 0.62, 0.85, 0.55); s.S(0xffffff, 0, 0.95, 0.2, 0.46, 0.65, 0.38); hy = 2.0; hz = 0.1; }
        else s.S(P, 0, 0.95, 0, 0.6 * fat, 0.55 * fat, 0.75 * fat);
        if (F.contains("longneck")) { s.C(P, 0, 1.7, 0.55, 0.11, 1.2).rx(-0.25); hy = 2.35; hz = 0.75; }
        s.S(P, 0, hy, hz, 0.38);
        double bl = F.contains("bigbeak") ? 0.65 : 0.3;
        s.K(F.contains("bigbeak") ? Q : 0xffa21a, 0, hy - 0.05, hz + 0.33 + bl / 2, 0.13, bl).rx(Math.PI / 2);
        s.eyes(0, hy + 0.08, hz + 0.3, 0.17, F.contains("owl") ? 0.14 : 0.085);
        for (int k = -1; k <= 1; k += 2) {
            if (F.contains("wings") || F.contains("ptero")) s.B(Q, k * 1.15, 1.15, 0, 1.5, 0.07, 0.75).rz(k * 0.3);
            else s.B(P, k * 0.6, 1.0, -0.05, 0.13, 0.42, 0.62).rz(k * 0.25);
            s.C(0xffa21a, k * 0.2, 0.22, 0, 0.05, 0.45);
            if (F.contains("owl")) s.K(P, k * 0.22, hy + 0.38, hz, 0.08, 0.28);
        }
        s.K(peng ? P : Q, 0, 1.0, -0.85, 0.22, 0.5).rx(-Math.PI / 2 + 0.4);
        if (F.contains("comb")) { s.B(0xff2020, 0, hy + 0.38, hz, 0.09, 0.28, 0.32); s.S(0xff2020, 0, hy - 0.3, hz + 0.3, 0.08, 0.12, 0.06); }
        if (F.contains("ptero")) s.K(P, 0, hy + 0.15, hz - 0.4, 0.1, 0.7).rx(-1.9);
    }

    private static void fish(Shape s, Set<String> F) {
        double b = F.contains("big") ? 1.45 : 1.0;
        boolean legs = F.contains("legs");
        s.push(0, legs ? 1.55 * b : 1.4, 0, 1);
        s.S(P, 0, 0, 0, 0.55 * b, 0.62 * b, 1.1 * b);
        s.S(Q, 0, -0.2 * b, 0.08, 0.47 * b, 0.42 * b, 0.92 * b);
        s.B(P, 0, 0, -1.3 * b, 0.12 * b, 0.9 * b, 0.6 * b);
        s.K(P, 0, 0.62 * b, -0.1 * b, 0.06, (F.contains("shark") ? 0.9 : 0.45) * b, 0.4 * b).rx(-0.3);
        for (int k = -1; k <= 1; k += 2) s.K(P, k * 0.55 * b, -0.15 * b, 0.2 * b, 0.05, 0.4 * b, 0.25 * b).rz(-k * 1.2);
        if (F.contains("cathead")) {
            s.B(Q, 0, 0.15, 1.05, 0.8, 0.7, 0.6);
            for (int k = -1; k <= 1; k += 2) s.K(Q, k * 0.25, 0.6, 1.05, 0.14, 0.35, 0.1);
            s.eyes(0, 0.25, 1.36, 0.18, 0.1);
            s.S(0xff8899, 0, 0.05, 1.36, 0.06);
        } else s.eyes(0, 0.18 * b, 0.9 * b, 0.3 * b, 0.11 * b);
        if (F.contains("whiskers")) for (int k = -1; k <= 1; k += 2) s.C(0x333333, k * 0.35, -0.15, 1.05, 0.02, 0.6).rz(k * 1.2);
        if (F.contains("sword")) s.K(0xdddddd, 0, 0, 1.75 * b, 0.06, 1.4).rx(Math.PI / 2);
        if (legs) for (int k = -1; k <= 1; k += 2) {
            s.B(Q, k * 0.28 * b, -0.95 * b, 0, 0.2 * b, 0.8 * b, 0.2 * b);
            s.B(0x2d6cff, k * 0.28 * b, -1.38 * b, 0.08 * b, 0.3 * b, 0.22 * b, 0.45 * b);
        }
        if (F.contains("spots")) for (int k = 0; k < 10; k++) {
            double th = k * 2.3, ph = 0.5 + (k % 4) * 0.25;
            s.S(0xffffff, 0.56 * b * Math.sin(ph) * Math.cos(th), 0.6 * b * Math.cos(ph), 1.1 * b * Math.sin(ph) * Math.sin(th), 0.07 * b);
        }
        if (F.contains("beluga")) s.S(P, 0, 0.3 * b, 0.75 * b, 0.42 * b, 0.38 * b, 0.4 * b);
        s.pop();
    }

    private static void squid(Shape s) {
        s.push(0, 0.6, 0, 1);
        s.S(P, 0, 2.1, 0, 0.7, 1.1, 0.7);
        for (int k = -1; k <= 1; k += 2) s.K(P, k * 0.6, 2.8, 0, 0.12, 0.6, 0.35).rz(-k * 0.9);
        s.S(P, 0, 1.15, 0, 0.62, 0.5, 0.62);
        s.eyes(0, 1.25, 0.55, 0.25, 0.13);
        for (int k = 0; k < 8; k++) {
            double a = k / 8.0 * Math.PI * 2;
            s.C(k % 2 == 1 ? Q : P, Math.cos(a) * 0.45, 0.55, Math.sin(a) * 0.45, 0.09, 1.2).rot(-Math.sin(a) * 0.5, 0, Math.cos(a) * 0.5);
        }
        s.pop();
    }

    private static void serpent(Shape s) {
        for (int k = 0; k < 12; k++) {
            double z = 1.2 - k * 0.42, y = 0.5 + Math.max(0, Math.sin(k * 0.75)) * 1.1, r = 0.48 - k * 0.025;
            s.S(k % 3 == 2 ? Q : P, 0, y, z, r);
            if (k % 2 == 0) s.K(Q, 0, y + r * 0.9, z, 0.1, 0.35);
        }
        s.B(P, 0, 1.0, 1.8, 0.75, 0.6, 1.0);
        s.B(Q, 0, 0.72, 1.85, 0.65, 0.2, 0.9);
        s.eyes(0, 1.18, 2.31, 0.25, 0.1);
        for (int k = -1; k <= 1; k += 2) s.K(Q, k * 0.5, 1.3, 1.5, 0.08, 0.6, 0.3).rz(-k * 0.8);
        for (int k = 0; k < 4; k++) s.K(0xffffff, -0.24 + k * 0.16, 0.85, 2.28, 0.04, 0.15);
    }

    private static void frog(Shape s) {
        s.S(P, 0, 0.6, 0, 0.85, 0.55, 0.8);
        s.S(Q, 0, 0.5, 0.25, 0.65, 0.4, 0.6);
        for (int k = -1; k <= 1; k += 2) {
            s.S(P, k * 0.35, 1.05, 0.42, 0.24);
            s.B(P, k * 0.7, 0.25, -0.3, 0.35, 0.3, 0.7);
            s.B(P, k * 0.45, 0.2, 0.55, 0.18, 0.4, 0.18);
        }
        s.eyes(0, 1.1, 0.62, 0.35, 0.13);
        s.B(0x222222, 0, 0.6, 0.8, 0.6, 0.04, 0.04);
    }

    private static void turtle(Shape s) {
        s.S(0xd9c58a, 0, 0.45, 0, 0.85, 0.25, 1.0);
        s.S(P, 0, 0.75, 0, 1.0, 0.55, 1.15);
        for (int k = 0; k < 6; k++) { double a = k / 6.0 * Math.PI * 2; s.S(0x2f5f2a, Math.cos(a) * 0.5, 1.1, Math.sin(a) * 0.55, 0.22, 0.1, 0.22); }
        s.S(0x2f5f2a, 0, 1.28, 0, 0.25, 0.1, 0.25);
        s.S(Q, 0, 0.7, 1.25, 0.33);
        s.eyes(0, 0.8, 1.55, 0.15, 0.08);
        for (int ix = -1; ix <= 1; ix += 2) for (int iz = -1; iz <= 1; iz += 2) s.S(Q, ix * 0.75, 0.3, iz * 0.7, 0.25, 0.22, 0.3);
    }

    private static void lizard(Shape s, Set<String> F, Random rnd) {
        boolean croc = F.contains("croc");
        s.B(P, 0, 0.55, 0, 0.85, 0.45, 1.7);
        s.B(Q, 0, 0.33, 0, 0.7, 0.06, 1.5);
        s.B(P, 0, 0.68, 1.1, 0.65, 0.45, 0.6);
        if (croc) {
            s.B(P, 0, 0.58, 1.75, 0.5, 0.25, 0.9);
            for (int k = 0; k < 5; k++) s.K(0xffffff, (k % 2 == 1 ? 0.2 : -0.2), 0.43, 1.4 + k * 0.2, 0.04, 0.12);
            for (int k = 0; k < 6; k++) s.B(P, 0, 0.82, 0.7 - k * 0.3, 0.3, 0.08, 0.15);
            s.eyes(0, 0.95, 1.3, 0.2, 0.1);
        } else s.eyes(0, 0.82, 1.41, 0.2, 0.09);
        s.K(P, 0, 0.5, -1.6, 0.32, 1.5).rx(-Math.PI / 2);
        for (int ix = -1; ix <= 1; ix += 2) for (int iz = -1; iz <= 1; iz += 2) s.B(P, ix * 0.55, 0.22, iz * 0.55, 0.28, 0.44, 0.28);
        if (F.contains("gills")) for (int k = -1; k <= 1; k += 2) for (int j = 0; j < 3; j++) s.K(Q, k * 0.38, 0.85 + j * 0.12, 0.95 - j * 0.05, 0.05, 0.35).rz(-k * (1.0 - j * 0.3));
        if (F.contains("spikes")) for (int k = 0; k < 6; k++) s.K(Q, 0, 0.88, 0.7 - k * 0.32, 0.08, 0.3);
        if (F.contains("stars")) { s.glow(0xffffff); for (int k = 0; k < 8; k++) s.S(0xffffff, rnd.nextDouble() * 0.8 - 0.4, 0.79, rnd.nextDouble() * 1.6 - 0.8, 0.05); }
    }

    private static void ape(Shape s, Set<String> F) {
        double sc = F.contains("big") ? 1.25 : 1.0;
        boolean skel = F.contains("skeleton"), pine = F.contains("pineapple");
        s.push(0, 0, 0, sc);
        for (int k = -1; k <= 1; k += 2) s.B(P, k * 0.32, 0.5, 0, 0.42, 1.0, 0.45);
        if (F.contains("bigfeet")) for (int k = -1; k <= 1; k += 2) s.B(Q, k * 0.38, 0.1, 0.25, 0.6, 0.2, 1.1);
        s.B(pine ? 0xf2c53d : P, 0, 1.65, 0, 1.15, 1.25, 0.85);
        if (pine) {
            for (int k = 0; k < 4; k++) s.B(0xb8860b, 0, 1.15 + k * 0.33, 0.43, 1.17, 0.05, 0.02);
            for (int k = -1; k <= 1; k++) s.B(0xb8860b, k * 0.35, 1.65, 0.43, 0.05, 1.25, 0.02);
        } else if (!skel) s.B(Q, 0, 1.6, 0.43, 0.7, 0.8, 0.02);
        if (skel) for (int k = 0; k < 4; k++) s.B(0x221a33, 0, 1.25 + k * 0.25, 0.44, 0.8, 0.06, 0.02);
        s.B(P, 0, 2.65, 0, 0.88, 0.82, 0.8);
        s.B(skel ? 0x111122 : Q, 0, 2.55, 0.41, 0.62, 0.48, 0.02);
        boolean prev = s.angry;
        if (skel) s.angry = true;
        s.eyes(0, 2.72, 0.42, 0.18, 0.1);
        s.angry = prev;
        for (int k = -1; k <= 1; k += 2) {
            s.S(P, k * 0.48, 2.65, 0, 0.13, 0.17, 0.1);
            s.B(P, k * 0.78, 1.4, 0, 0.32, 1.35, 0.36).rz(k * 0.12);
        }
        if (F.contains("leaves") || pine) for (int k = 0; k < 7; k++) {
            double a = k / 7.0 * Math.PI * 2;
            s.K(pine ? 0x2e8b3a : 0x3cae3c, Math.cos(a) * 0.3, 3.3, Math.sin(a) * 0.3, 0.15, pine ? 0.9 : 0.7).rot(Math.sin(a) * 0.5, 0, -Math.cos(a) * 0.5);
        }
        if (F.contains("leaves")) s.S(0xd9a066, 0, 2.5, 0.55, 0.18, 0.15, 0.25);
        s.pop();
    }

    private static void blob(Shape s) {
        for (int k = -1; k <= 1; k += 2) s.C(Q, k * 0.3, 0.5, 0, 0.1, 1.0);
        s.C(P, 0, 1.6, 0, 0.6, 1.3);
        for (int k = 0; k < 3; k++) s.C(Q, 0, 1.1 + k * 0.45, 0, 0.62, 0.08);
        s.S(P, 0, 2.65, 0, 0.5);
        s.eyes(0, 2.75, 0.45, 0.17, 0.1);
        s.B(0x331a0a, 0, 2.45, 0.48, 0.35, 0.08, 0.04);
        for (int k = -1; k <= 1; k += 2) s.C(Q, k * 0.8, 1.7, 0, 0.08, 1.1).rz(k * 0.6);
        s.C(0x8b5a2b, 1.25, 2.3, 0.2, 0.06, 1.0).rz(-0.4);
    }

    private static void snake(Shape s, Set<String> F) {
        for (int k = 0; k < 11; k++) {
            double r = 0.36 - k * 0.018;
            s.S((F.contains("stripes") && k % 2 == 1) ? Q : P, Math.sin(k * 0.75) * 0.55, r, 0.6 - k * 0.36, r * 1.05, r, r * 1.1);
        }
        s.S(P, 0, 0.45, 1.1, 0.4, 0.33, 0.5);
        s.eyes(0, 0.62, 1.52, 0.17, 0.09);
        s.B(0xff2040, 0, 0.38, 1.7, 0.05, 0.03, 0.35);
        if (F.contains("crown")) s.crown(0, 0.85, 1.05, 0.25);
    }

    private static void spider(Shape s, Set<String> F) {
        boolean sc = F.contains("scorpion");
        if (!sc) { s.S(P, 0, 0.9, -0.55, 0.62, 0.52, 0.72); s.S(Q, 0, 1.12, -0.6, 0.25, 0.15, 0.3); }
        else s.B(P, 0, 0.55, 0, 0.8, 0.35, 1.3);
        s.S(P, 0, 0.75, 0.35, 0.4, 0.34, 0.42);
        s.eyes(0, 0.85, 0.72, 0.14, 0.08);
        s.glow(0xff2020);
        for (int k = -1; k <= 1; k += 2) s.S(0xff2020, k * 0.07, 1.02, 0.66, 0.04);
        for (int k = -1; k <= 1; k += 2)
            for (int j = 0; j < 4; j++) {
                double z = 0.45 - j * 0.28;
                s.B(P, k * 0.65, 0.85, z, 0.75, 0.07, 0.07).rz(k * 0.5);
                s.B(P, k * 1.08, 0.5, z, 0.07, 0.95, 0.07).rz(k * 0.3);
            }
        if (sc) {
            for (int k = -1; k <= 1; k += 2) { s.B(P, k * 0.45, 0.55, 0.9, 0.15, 0.15, 0.6); s.B(Q, k * 0.5, 0.55, 1.3, 0.3, 0.2, 0.35); }
            double[][] tail = {{0, 0.65, -0.8}, {0, 0.98, -1.05}, {0, 1.38, -1.1}, {0, 1.72, -0.95}, {0, 1.95, -0.65}};
            for (double[] p : tail) s.S(P, p[0], p[1], p[2], 0.2);
            s.K(Q, 0, 1.9, -0.38, 0.08, 0.38).rx(Math.PI / 2 + 0.6);
        }
    }

    private static void dragon(Shape s, Set<String> F) {
        s.S(P, 0, 1.35, 0, 0.8, 0.72, 1.25);
        s.S(Q, 0, 1.15, 0.15, 0.6, 0.5, 1.0);
        for (int ix = -1; ix <= 1; ix += 2) for (int iz = -1; iz <= 1; iz += 2) s.B(P, ix * 0.5, 0.45, iz * 0.65, 0.3, 0.9, 0.32);
        s.C(P, 0, 2.05, 0.95, 0.22, 1.1).rx(0.65);
        s.B(P, 0, 2.6, 1.4, 0.6, 0.5, 0.75);
        s.B(P, 0, 2.48, 1.9, 0.42, 0.3, 0.45);
        s.eyes(0, 2.72, 1.78, 0.2, 0.09);
        for (int k = -1; k <= 1; k += 2) {
            s.K(0xfff0d0, k * 0.2, 2.95, 1.15, 0.07, 0.45).rx(-0.6);
            s.B(Q, k * 1.4, 2.05, -0.1, 2.0, 0.06, 1.2).rz(k * 0.35);
            s.B(P, k * 1.4, 2.1, 0.45, 2.0, 0.1, 0.1).rz(k * 0.35);
        }
        s.K(P, 0, 1.15, -1.75, 0.32, 1.8).rx(-Math.PI / 2 - 0.15);
        for (int k = 0; k < 5; k++) s.K(Q, 0, 2.0, 0.45 - k * 0.4, 0.08, 0.3);
        if (F.contains("moon")) { s.glow(0xfff8d0); s.S(0xfff8d0, 1.3, 3.4, 0, 0.28); }
    }

    private static void dino(Shape s, Set<String> F, Random rnd) {
        if (F.contains("trex")) {
            for (int k = -1; k <= 1; k += 2) { s.B(P, k * 0.38, 0.75, -0.15, 0.42, 1.5, 0.55); s.B(Q, k * 0.38, 0.08, 0.05, 0.5, 0.16, 0.8); }
            s.S(P, 0, 1.95, 0, 0.85, 0.85, 1.25).rx(-0.25);
            s.S(Q, 0, 1.8, 0.35, 0.6, 0.6, 0.8);
            s.K(P, 0, 1.85, -1.7, 0.45, 1.9).rx(-Math.PI / 2 - 0.15);
            s.B(P, 0, 2.85, 1.15, 0.75, 0.7, 1.15);
            s.B(Q, 0, 2.42, 1.2, 0.65, 0.2, 1.0);
            for (int k = 0; k < 5; k++) s.K(0xffffff, -0.28 + k * 0.14, 2.5, 1.7, 0.04, 0.14);
            s.eyes(0, 3.05, 1.73, 0.24, 0.1);
            for (int k = -1; k <= 1; k += 2) s.B(P, k * 0.45, 1.95, 0.85, 0.12, 0.45, 0.12).rx(0.7);
        } else if (F.contains("bronto")) {
            s.S(P, 0, 1.6, 0, 1.0, 0.85, 1.4);
            for (int ix = -1; ix <= 1; ix += 2) for (int iz = -1; iz <= 1; iz += 2) s.C(P, ix * 0.55, 0.6, iz * 0.8, 0.25, 1.2);
            for (int k = 0; k < 6; k++) s.S(P, 0, 2.1 + k * 0.32, 0.9 + k * 0.22, 0.28 - k * 0.015);
            s.S(P, 0, 4.05, 2.3, 0.32, 0.28, 0.42);
            s.eyes(0, 4.15, 2.66, 0.15, 0.07);
            for (int k = 0; k < 7; k++) s.S(P, 0, 1.5 - k * 0.15, -1.3 - k * 0.35, 0.35 - k * 0.04);
            for (int k = 0; k < 6; k++) s.S(Q, rnd.nextDouble() - 0.5, 2.3, rnd.nextDouble() * 1.6 - 0.8, 0.15, 0.06, 0.15);
        } else if (F.contains("trike")) {
            s.S(P, 0, 1.25, 0, 0.95, 0.75, 1.3);
            for (int ix = -1; ix <= 1; ix += 2) for (int iz = -1; iz <= 1; iz += 2) s.B(P, ix * 0.55, 0.4, iz * 0.75, 0.35, 0.8, 0.38);
            s.B(P, 0, 1.3, 1.45, 0.75, 0.65, 0.8);
            s.B(Q, 0, 1.8, 1.05, 1.5, 1.1, 0.12).rx(-0.3);
            for (int k = -1; k <= 1; k += 2) s.K(0xfff8e0, k * 0.25, 1.6, 1.9, 0.07, 0.6).rx(Math.PI / 2 - 0.3);
            s.K(0xfff8e0, 0, 1.2, 1.9, 0.06, 0.3).rx(Math.PI / 2);
            s.eyes(0, 1.45, 1.86, 0.25, 0.09);
            s.K(P, 0, 1.1, -1.6, 0.3, 1.2).rx(-Math.PI / 2);
        } else {
            s.S(P, 0, 0.95, 0, 1.05, 0.55, 1.35);
            for (int a = -1; a <= 1; a++) for (int c = -2; c <= 2; c++) s.K(Q, a * 0.45, 1.38, c * 0.4, 0.1, 0.35);
            for (int ix = -1; ix <= 1; ix += 2) for (int iz = -1; iz <= 1; iz += 2) s.B(P, ix * 0.6, 0.3, iz * 0.7, 0.3, 0.6, 0.3);
            s.B(P, 0, 0.85, 1.45, 0.55, 0.42, 0.55);
            s.eyes(0, 0.95, 1.73, 0.18, 0.08);
            s.C(P, 0, 0.8, -1.7, 0.12, 1.0).rx(Math.PI / 2);
            s.S(Q, 0, 0.8, -2.25, 0.3, 0.25, 0.3);
        }
    }

    private static void pepper(Shape s) {
        for (int k = -1; k <= 1; k += 2) { s.B(0x2e8b2e, k * 0.18, 0.35, 0, 0.12, 0.7, 0.12); s.B(0xffffff, k * 0.18, 0.05, 0.06, 0.2, 0.12, 0.32); }
        s.S(P, 0, 1.35, 0, 0.5, 0.85, 0.5);
        s.K(P, 0, 0.62, 0.05, 0.38, 0.6);
        s.S(Q, 0, 2.15, 0, 0.38, 0.14, 0.38);
        s.C(Q, 0, 2.4, 0, 0.07, 0.35);
        s.B(0x111111, 0, 1.65, 0.45, 0.75, 0.12, 0.06);
        for (int k = -1; k <= 1; k += 2) s.B(0x111111, k * 0.18, 1.6, 0.47, 0.3, 0.2, 0.04);
        s.B(0x330000, 0, 1.3, 0.49, 0.25, 0.05, 0.03);
        for (int k = -1; k <= 1; k += 2) s.B(0x2e8b2e, k * 0.58, 1.35, 0, 0.1, 0.6, 0.1).rz(k * 0.7);
    }

    private static void worm(Shape s) {
        for (int k = 0; k < 8; k++) {
            double z = 0.9 - k * 0.42;
            s.S(k % 2 == 1 ? Q : P, 0, 0.45, z, 0.36, 0.32, 0.3);
            for (int j = -1; j <= 1; j += 2) s.B(Q, j * 0.38, 0.18, z, 0.25, 0.06, 0.06).rz(-j * 0.5);
        }
        s.S(P, 0, 0.6, 1.35, 0.38);
        s.eyes(0, 0.72, 1.68, 0.15, 0.08);
        for (int j = -1; j <= 1; j += 2) { s.C(Q, j * 0.15, 1.1, 1.45, 0.025, 0.6).rz(-j * 0.4); s.S(Q, j * 0.27, 1.38, 1.45, 0.06); }
    }
}
