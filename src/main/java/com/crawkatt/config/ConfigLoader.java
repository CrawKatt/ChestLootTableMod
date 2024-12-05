package com.crawkatt.config;

import com.crawkatt.ChestLootMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {
    private static final Path CONFIG_PATH = Path.of("config", "chestloot.json");
    private static final String DEFAULT_KEY = "default";
    private static final String BIOMES_KEY = "biomes";

    private static Map<String, List<Identifier>> biomeLootMap = new HashMap<>();
    public static List<Identifier> defaultLoot = new ArrayList<>();

    public static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) createDefaultConfig();

            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                loadDefaultLoot(json);
                loadBiomeLoot(json);
            }
        } catch (Exception why) {
            ChestLootMod.LOGGER.error("Error al cargar la configuración de chestloot.json: ", why);
        }
    }

    private static void loadDefaultLoot(JsonObject json) {
        defaultLoot = new ArrayList<>();
        JsonArray defaultArray = json.getAsJsonArray(DEFAULT_KEY);
        if (defaultArray != null) defaultArray.forEach(e -> defaultLoot.add(new Identifier(e.getAsString())));
    }

    private static void loadBiomeLoot(JsonObject json) {
        biomeLootMap = new HashMap<>();
        JsonObject biomes = json.getAsJsonObject(BIOMES_KEY);
        if (biomes != null) {
            for (String biome : biomes.keySet()) {
                List<Identifier> lootTables = new ArrayList<>();
                JsonArray lootArray = biomes.getAsJsonArray(biome);
                if (lootArray != null) lootArray.forEach(e -> lootTables.add(new Identifier(e.getAsString())));
                biomeLootMap.put(biome, lootTables);
            }
        }
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
                writer.write("""
                {
                  "default": [],
                  "biomes": {}
                }
                """);
            }
        } catch (IOException why) {
            ChestLootMod.LOGGER.error("Error al crear el archivo predeterminado chestloot.json", why);
        }
    }

    public static List<Identifier> getLootTables(String biome) {
        return biomeLootMap.getOrDefault(biome, defaultLoot);
    }
}
