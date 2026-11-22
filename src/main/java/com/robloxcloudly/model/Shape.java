package com.robloxcloudly.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kreator modeli z klocków. Współrzędne: y w górę, przód = +z (południe), 1 jednostka = pół bloku przy skali 1.
 * Kule/walce/stożki są zamieniane na prostopadłościany (styl Minecraft).
 * Każdy klocek staje się potem jednym block_display.
 */
public class Shape {
    public static final int P = -2, Q = -3;   // kolor główny, kolor drugi

    public static final class Piece {
        public int color;
        public boolean glow;
        public float cx, cy, cz, w, h, d, rx, ry, rz;
        public Piece rx(double a) { rx = (float) a; return this; }
        public Piece ry(double a) { ry = (float) a; return this; }
        public Piece rz(double a) { rz = (float) a; return this; }
        public Piece rot(double x, double y, double z) { rx = (float) x; ry = (float) y; rz = (float) z; return this; }
    }

    public record Model(List<Piece> pieces, float top) {}

    private final List<Piece> pieces = new ArrayList<>();
    private float ox, oy, oz, sc = 1.0F;
    private final Deque<float[]> stack = new ArrayDeque<>();
    public int c1 = 0xffffff, c2 = 0x888888;
    public boolean glowMain, angry;
    private final Set<Integer> glowSet = new HashSet<>();

    public void glow(int color) { glowSet.add(color); }

    public void push(double x, double y, double z, double s) {
        stack.push(new float[]{ox, oy, oz, sc});
        ox += sc * (float) x; oy += sc * (float) y; oz += sc * (float) z;
        sc *= (float) s;
    }

    public void pop() {
        float[] t = stack.pop();
        ox = t[0]; oy = t[1]; oz = t[2]; sc = t[3];
    }

    public Piece B(int col, double x, double y, double z, double w, double h, double d) {
        Piece p = new Piece();
        p.color = col;
        p.cx = ox + sc * (float) x; p.cy = oy + sc * (float) y; p.cz = oz + sc * (float) z;
        p.w = (float) w * sc; p.h = (float) h * sc; p.d = (float) d * sc;
        pieces.add(p);
        return p;
    }

    public Piece S(int col, double x, double y, double z, double a, double b, double c) { return B(col, x, y, z, a * 1.7, b * 1.7, c * 1.7); }
    public Piece S(int col, double x, double y, double z, double a) { return S(col, x, y, z, a, a, a); }
    public Piece C(int col, double x, double y, double z, double r, double h) { return B(col, x, y, z, r * 1.8, h, r * 1.8); }
    public Piece K(int col, double x, double y, double z, double r, double h) { return B(col, x, y, z, r * 1.2, h, r * 1.2); }
    public Piece K(int col, double x, double y, double z, double r, double h, double r2) { return B(col, x, y, z, r * 1.2, h, r2 * 1.2); }

    public void eyes(double x, double y, double z, double sep, double r) {
        int pupil = angry ? 0xff1a1a : 0x111111;
        if (angry) glow(pupil);
        for (int s = -1; s <= 1; s += 2) {
            S(0xffffff, x + s * sep, y, z, r, r, r * 0.6);
            S(pupil, x + s * sep, y, z + r * 0.45, r * 0.55, r * 0.55, r * 0.4);
        }
    }

    public void crown(double x, double y, double z, double r) {
        int gold = 0xffd700;
        C(gold, x, y, z, r, r * 0.6);
        for (int k = 0; k < 5; k++) {
            double a = k / 5.0 * Math.PI * 2;
            K(gold, x + Math.cos(a) * r * 0.8, y + r * 0.55, z + Math.sin(a) * r * 0.8, r * 0.18, r * 0.5);
        }
        glow(0xff2050);
        S(0xff2050, x, y, z + r, r * 0.16);
    }

    public Model bake() {
        float top = 0;
        List<Piece> out = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.color == P) { p.color = c1; p.glow = glowMain; }
            else if (p.color == Q) { p.color = c2; p.glow = glowMain; }
            else p.glow = glowSet.contains(p.color);
            top = Math.max(top, p.cy + p.h / 2);
            out.add(p);
        }
        return new Model(out, top);
    }
}
