package com.crawkatt.config;

import com.crawkatt.ChestLootMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigLoader {
    private static final Path CONFIG_PATH = Path.of("config", "chestloot.json");
    private static final String DEFAULT_KEY = "default";
    private static final String BIOMES_KEY = "biomes";

    private static Map<String, List<Identifier>> biomeLootMap = new HashMap<>();
    public static List<Identifier> defaultLoot = new ArrayList<>();

    public static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) createDefaultConfig();
            readConfig();
        } catch (Exception why) {
            ChestLootMod.LOGGER.error("Error al cargar la configuración de chestloot.json: ", why);
        }
    }

    private static void readConfig() throws IOException {
        try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            loadDefaultLoot(json);
            loadBiomeLoot(json);
        }
    }

    private static void loadDefaultLoot(JsonObject json) {
        defaultLoot = new ArrayList<>();
        Optional.ofNullable(json.getAsJsonArray(DEFAULT_KEY))
                .ifPresent(defaultArray -> defaultArray.forEach(element -> defaultLoot.add(new Identifier(element.getAsString()))));
    }

    private static void loadBiomeLoot(JsonObject json) {
        biomeLootMap = new HashMap<>();
        Optional.ofNullable(json.getAsJsonObject(BIOMES_KEY))
                .ifPresent(biomes -> biomes.entrySet().forEach(entry -> {
                    String biome = entry.getKey();
                    List<Identifier> lootTables = new ArrayList<>();
                    Optional.ofNullable(entry.getValue().getAsJsonArray())
                            .ifPresent(lootArray -> lootArray.forEach(element -> lootTables.add(new Identifier(element.getAsString()))));
                    biomeLootMap.put(biome, lootTables);
                }));
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            createDefaultJson();
        } catch (IOException why) {
            ChestLootMod.LOGGER.error("Error al crear el archivo predeterminado chestloot.json", why);
        }
    }

    private static void createDefaultJson() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonObject();

        root.add(DEFAULT_KEY, gson.toJsonTree(new ArrayList<>()));
        root.add(BIOMES_KEY, new JsonObject());

        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            gson.toJson(root, writer);
        }
    }

    public static List<Identifier> getLootTables(String biome) {
        return biomeLootMap.getOrDefault(biome, defaultLoot);
    }
}
