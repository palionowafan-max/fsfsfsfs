package com.robloxcloudly;

import net.minecraft.ChatFormatting;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Wszystkie dane gry: światy, pety, rzadkości, bieżnie. */
public final class Defs {
    private Defs() {}

    public static final String[] RAR = {"Common", "Uncommon", "Rare", "Epic", "Legendary", "Mythic", "Cosmic", "Secret", "Eternal"};
    public static final ChatFormatting[] RFMT = {ChatFormatting.GRAY, ChatFormatting.GREEN, ChatFormatting.BLUE, ChatFormatting.LIGHT_PURPLE,
            ChatFormatting.GOLD, ChatFormatting.RED, ChatFormatting.AQUA, ChatFormatting.DARK_GRAY, ChatFormatting.YELLOW};
    public static final int[] RCOL = {0xb8b8b8, 0x5ee36b, 0x3d9bff, 0xb65cff, 0xffb72b, 0xff3d6e, 0x2fe8ff, 0x262626, 0xfff6a0};
    public static final long[] INC = {1, 3, 8, 20, 60, 180, 550, 1600, 5000};
    public static final double[] WGT = {40, 25, 16, 10, 5, 2.5, 1, 0.4, 0.12};

    public record PetDef(String name, int world, int rarity, String type, int c1, int c2, Set<String> flags) {}

    public record WorldDef(String name, int req, String boss, int bossSpeed, float bossScale, int spot, List<PetDef> eggs) {}

    public record Tread(String name, double rate, long cost) {}

    public static final Map<String, PetDef> PETS = new LinkedHashMap<>();
    public static final List<WorldDef> WORLDS = new ArrayList<>();
    public static final Tread[] TREAD = {
            new Tread("Drewniana bieżnia", 0.6, 0),
            new Tread("Kamienna bieżnia", 1.5, 150),
            new Tread("Żelazna bieżnia", 4, 1500),
            new Tread("Złota bieżnia", 10, 12000),
            new Tread("Diamentowa bieżnia", 25, 90000),
            new Tread("Lawowa bieżnia", 60, 600000),
            new Tread("Kosmiczna bieżnia", 140, 4000000),
    };

    private static List<PetDef> cur;
    private static int curW;

    private static void world(String name, int req, String boss, int bossSpeed, float scale, int spot) {
        cur = new ArrayList<>();
        curW = WORLDS.size();
        WORLDS.add(new WorldDef(name, req, boss, bossSpeed, scale, spot, cur));
    }

    private static void pet(int r, String name, String type, int c1, int c2, String flags) {
        Set<String> f = new HashSet<>();
        for (String s : flags.split(",")) if (!s.isBlank()) f.add(s.trim());
        PetDef d = new PetDef(name, curW, r, type, c1, c2, f);
        cur.add(d);
        PETS.put(name, d);
    }

    static {
        world("Las", 0, "Bear", 15, 3.0F, 0x6b4423);
        pet(0, "Chicken", "bird", 0xffffff, 0xff9a1a, "comb");
        pet(0, "Dog", "quad", 0xc8935a, 0x7a4a20, "floppy");
        pet(1, "Bird", "bird", 0x3fa9f5, 0xffd400, "");
        pet(2, "Burrowing Owl", "bird", 0xa67c52, 0xf2e3c6, "owl");
        pet(2, "Raccoon", "quad", 0x8e8e8e, 0x333333, "mask,ringtail");
        pet(3, "Fox", "quad", 0xff7a1a, 0xffffff, "pointy,bushy");
        pet(3, "Bear", "quad", 0x6b4423, 0x3d2614, "round,big");
        pet(4, "Brr Brr Patapim", "ape", 0x6fae3a, 0x8b5a2b, "leaves,bigfeet");

        world("Jezioro", 30, "Leviathan", 33, 2.8F, 0x2a7fff);
        pet(0, "Frog", "frog", 0x5cc93b, 0xd9f7c0, "");
        pet(0, "Duckling", "bird", 0xffe14d, 0xff9900, "");
        pet(1, "Catfish", "fish", 0x6b7b8c, 0xc9b28a, "whiskers");
        pet(2, "Turtle", "turtle", 0x3f7f3a, 0x9fd27a, "");
        pet(3, "Trulimero Trulicina", "fish", 0x6fb7ff, 0xffb46b, "cathead,legs");
        pet(3, "Swan", "bird", 0xffffff, 0xff8c1a, "longneck");
        pet(4, "Axolotl", "lizard", 0xffaad4, 0xff5fa2, "gills");
        pet(6, "Leviathan", "serpent", 0x1f6fbf, 0x7fe0ff, "jaws");

        world("Pustynia", 55, "Royal Sphinx", 60, 2.8F, 0xc98a3a);
        pet(0, "Jerboa", "quad", 0xe8c48a, 0xffffff, "bigears,small,longtail");
        pet(1, "Fennec", "quad", 0xf0c27a, 0xffffff, "hugeears,bushy");
        pet(2, "Camel", "quad", 0xc9a063, 0xa07a40, "hump,long");
        pet(3, "Tob Tobi Tob Tob", "blob", 0xd9a35b, 0x6b3e1f, "");
        pet(4, "Snake", "snake", 0xd4b13a, 0x7a5c12, "stripes");
        pet(5, "Sand Spider", "spider", 0xd8b07a, 0x7a5a30, "");
        pet(5, "Scorpion", "spider", 0x2b2b2b, 0xff6a00, "scorpion");
        pet(6, "Royal Sphinx", "quad", 0xe8c25a, 0x1f4fa8, "sphinx,big");

        world("Dżungla", 90, "King Snake", 99, 3.6F, 0x1f6f1f);
        pet(2, "Chimpanzee", "ape", 0x3d2b1f, 0xd9b38c, "");
        pet(2, "Toucan", "bird", 0x111111, 0xff8800, "bigbeak");
        pet(3, "Crocodile", "lizard", 0x3f6b2a, 0xb3c97a, "croc");
        pet(4, "Gorilla", "ape", 0x2a2a2a, 0x555555, "big");
        pet(4, "Orangutini Ananassini", "ape", 0xd9822b, 0xf2d13d, "pineapple");
        pet(5, "Spider", "spider", 0x222222, 0xff2a2a, "");
        pet(5, "Tiger", "quad", 0xff8a1f, 0x111111, "stripes,round");
        pet(7, "King Snake", "snake", 0x1a1a1a, 0xffcc00, "crown,stripes");

        world("Śnieżna Kraina", 140, "Yeti", 154, 2.6F, 0x9fd6ff);
        pet(2, "Penguin", "bird", 0x1c1c2b, 0xffffff, "penguin");
        pet(3, "Walrus", "quad", 0x8a6a55, 0xffffff, "tusks,fat");
        pet(4, "Polar Bear", "quad", 0xf4f8ff, 0xcfd8e3, "round,big");
        pet(5, "Sabertooth Tiger", "quad", 0xe0a050, 0xffffff, "saber,round");
        pet(5, "Mammoth", "quad", 0x7a4a2a, 0xf5f0e0, "trunk,tusks,big");
        pet(6, "King Mammoth", "quad", 0x5a3a8a, 0xffd700, "trunk,tusks,big,crown");
        pet(7, "Yeti", "ape", 0xf0f6ff, 0x8fc3ff, "big");
        pet(8, "Ice Dragon", "dragon", 0x9fe6ff, 0xffffff, "glow");

        world("Wulkan", 210, "Lava Dragon", 231, 2.6F, 0xff4500);
        pet(2, "Lava Gecko", "lizard", 0xff5a1f, 0xffd000, "glow");
        pet(3, "Lava Frog", "frog", 0xff3d00, 0x2b1a12, "glow");
        pet(4, "Flaming Bull", "quad", 0x6b1a0a, 0xff9900, "horns,fire");
        pet(4, "Lava Iguana", "lizard", 0xb32400, 0xff9900, "spikes,glow");
        pet(5, "Chillin Chilli", "pepper", 0xe0201a, 0x2e8b2e, "");
        pet(7, "Cerberus", "quad", 0x2a1a1a, 0xff3300, "threeheads,pointy,fire");
        pet(8, "Phoenix", "bird", 0xff6a00, 0xffd000, "fire,wings");
        pet(8, "Lava Dragon", "dragon", 0x8b1a00, 0xff7a00, "fire");

        world("Otchłań Oceanu", 300, "Kraken", 330, 2.6F, 0x00bfff);
        pet(2, "Parrotfish", "fish", 0x2fd6a0, 0xff6ec7, "");
        pet(3, "Swordfish", "fish", 0x2a4f8f, 0xc0d8ff, "sword");
        pet(4, "Shark", "fish", 0x6f8aa3, 0xffffff, "shark");
        pet(5, "Orca", "fish", 0x111111, 0xffffff, "shark,big");
        pet(6, "Whale Shark", "fish", 0x3a5f80, 0xeaf2ff, "big,spots");
        pet(6, "Beluga Whale", "fish", 0xf2f6ff, 0xdde6f0, "big,beluga");
        pet(7, "Kraken", "squid", 0x8b1a5a, 0xff7ac0, "");
        pet(8, "El Maja", "squid", 0x1affc6, 0x0a4a7a, "glow");

        world("Prehistoria", 420, "T-Rex", 462, 2.2F, 0x8b6a3a);
        pet(2, "Dodo", "bird", 0x8a8f99, 0xe8d36b, "fat,bigbeak");
        pet(4, "Pterodactyl", "bird", 0x9c6b3a, 0xe0b080, "ptero");
        pet(5, "Ankylosaurus", "dino", 0x7a8a3a, 0x4a5a1a, "ankylo");
        pet(6, "Triceratops", "dino", 0x4f8a9f, 0xf2e6c0, "trike");
        pet(6, "Bronto", "dino", 0x6f9f4a, 0xc9e0a0, "bronto");
        pet(7, "T-Rex", "dino", 0x4a7a2a, 0xd0e0a0, "trex");
        pet(7, "Tralaledon", "fish", 0x3f8fd0, 0xffffff, "shark,legs,big");
        pet(8, "Mosasaurus", "serpent", 0x2f5f7f, 0xa0d0e0, "jaws");

        world("Kosmos", 600, "Cosmic Skeleton Boss", 660, 2.4F, 0xff4af0);
        pet(3, "Centapede", "worm", 0x7a3aff, 0x00e5ff, "");
        pet(4, "Cosmic Gecko", "lizard", 0x3a1aff, 0x00ffd5, "glow,stars");
        pet(5, "Cosmic Gorilla", "ape", 0x2a0a5a, 0xc04aff, "big,glow");
        pet(6, "La Vacca Saturno Saturnita", "quad", 0xffffff, 0x222222, "cow,ring");
        pet(7, "Cosmic Skeleton Boss", "ape", 0xeeeeff, 0x7a3aff, "skeleton,glow,big");
        pet(7, "Cosmic Dragon", "dragon", 0x4a1aa0, 0xff4af0, "glow");
        pet(8, "Eternal Lunar Dragon", "dragon", 0xe0e8ff, 0x9ad0ff, "glow,moon");
    }

    public static PetDef get(String name) {
        PetDef d = PETS.get(name);
        return d != null ? d : PETS.get("Chicken");
    }

    public static long income(PetDef d) {
        return Math.round(INC[d.rarity()] * (1 + d.world() * 0.6));
    }

    public static PetDef pick(int world, RandomSource rnd) {
        List<PetDef> list = WORLDS.get(world).eggs();
        double tot = 0;
        for (PetDef d : list) tot += WGT[d.rarity()];
        double x = rnd.nextDouble() * tot;
        for (PetDef d : list) {
            x -= WGT[d.rarity()];
            if (x <= 0) return d;
        }
        return list.get(0);
    }

    /** Statystyka szybkości -> mnożnik prędkości (16 = normalnie). */
    public static double velOf(double s) {
        return 16.0 * Math.pow(Math.max(s, 1.0) / 16.0, 0.6);
    }

    public static String fmt(double n) {
        long v = (long) Math.floor(n);
        String[] u = {"", "K", "M", "B", "T", "Qa", "Qi"};
        int i = 0;
        double x = v;
        while (x >= 1000 && i < u.length - 1) { x /= 1000; i++; }
        if (i == 0) return Long.toString(v);
        return (x < 10 ? String.format(Locale.ROOT, "%.2f", x) : x < 100 ? String.format(Locale.ROOT, "%.1f", x) : Long.toString((long) x)) + u[i];
    }
}
