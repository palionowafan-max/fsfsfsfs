package com.robloxcloudly.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.robloxcloudly.Defs;
import net.minecraft.world.level.storage.LevelResource;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Postęp gracza – zapisywany w pliku world/robloxcloudly/players/<uuid>.json */
public final class PlayerData {
    public static final int MAX_PETS = 30;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerData> CACHE = new HashMap<>();

    public double coins = 0;
    public double speed = 16;
    public double pending = 0;
    public int tread = 0;
    public List<String> pets = new ArrayList<>();
    public Set<String> found = new LinkedHashSet<>();

    private static Path dir() {
        return GameManager.server.getWorldPath(LevelResource.ROOT).resolve("robloxcloudly").resolve("players");
    }

    public static PlayerData get(UUID id) {
        PlayerData d = CACHE.get(id);
        if (d != null) return d;
        d = new PlayerData();
        try {
            Path f = dir().resolve(id + ".json");
            if (Files.exists(f)) {
                try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                    PlayerData loaded = GSON.fromJson(r, PlayerData.class);
                    if (loaded != null) d = loaded;
                }
            }
        } catch (Exception e) {
            GameManager.LOGGER.warn("[robloxcloudly] Nie udało się wczytać danych gracza {}", id, e);
        }
        if (d.pets == null) d.pets = new ArrayList<>();
        if (d.found == null) d.found = new LinkedHashSet<>();
        CACHE.put(id, d);
        return d;
    }

    public static void save(UUID id) {
        PlayerData d = CACHE.get(id);
        if (d == null || GameManager.server == null) return;
        try {
            Files.createDirectories(dir());
            try (Writer w = Files.newBufferedWriter(dir().resolve(id + ".json"), StandardCharsets.UTF_8)) {
                GSON.toJson(d, w);
            }
        } catch (Exception e) {
            GameManager.LOGGER.warn("[robloxcloudly] Nie udało się zapisać danych gracza {}", id, e);
        }
    }

    public static void unload(UUID id) {
        save(id);
        CACHE.remove(id);
    }

    public static void saveAll() {
        for (UUID id : new ArrayList<>(CACHE.keySet())) save(id);
    }

    public static void reset(UUID id) {
        CACHE.put(id, new PlayerData());
        save(id);
    }

    public long income() {
        long sum = 0;
        for (int i = 0; i < Math.min(MAX_PETS, pets.size()); i++) sum += Defs.income(Defs.get(pets.get(i)));
        return sum;
    }
}
