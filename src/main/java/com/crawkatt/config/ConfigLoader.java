package com.crawkatt.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {
    private static final Path CONFIG_PATH = Path.of("config", "chestloot.json");
    private static Map<String, List<Identifier>> biomeLootMap = new HashMap<>();
    private static List<Identifier> defaultLoot = new ArrayList<>();

    public static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                createDefaultConfig();
            }

            JsonObject json = JsonParser.parseReader(new FileReader(CONFIG_PATH.toFile())).getAsJsonObject();

            defaultLoot = new ArrayList<>();
            json.getAsJsonArray("default").forEach(e -> defaultLoot.add(new Identifier(e.getAsString())));

            // Cargar biomas y sus Loot Tables
            biomeLootMap = new HashMap<>();
            JsonObject biomes = json.getAsJsonObject("biomes");
            for (String biome : biomes.keySet()) {
                List<Identifier> lootTables = new ArrayList<>();
                biomes.getAsJsonArray(biome).forEach(e -> lootTables.add(new Identifier(e.getAsString())));
                biomeLootMap.put(biome, lootTables);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la configuración de chestloot.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, """
            {
              "default": [],
              "biomes": {}
            }
        """);
        } catch (Exception e) {
            System.err.println("Error al crear el archivo predeterminado chestloot.json: " + e.getMessage());
        }
    }

    public static List<Identifier> getLootTables(String biome) {
        return biomeLootMap.getOrDefault(biome, defaultLoot);
    }
}
