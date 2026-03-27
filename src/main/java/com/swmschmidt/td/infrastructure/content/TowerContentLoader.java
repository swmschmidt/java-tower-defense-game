package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.tower.TowerDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class TowerContentLoader {

    public TowerCatalog load(Path filePath) {
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(filePath)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load tower content from " + filePath, exception);
        }

        String[] ids = required(properties, "tower.ids").split(";");
        Map<String, TowerDefinition> definitionsById = new LinkedHashMap<>();

        for (String idToken : ids) {
            String id = idToken.trim();
            if (id.isBlank()) {
                continue;
            }

            String prefix = "tower." + id + ".";
            TowerDefinition definition = new TowerDefinition(
                id,
                doubleProperty(properties, prefix + "range_units"),
                doubleProperty(properties, prefix + "damage_per_shot"),
                doubleProperty(properties, prefix + "attacks_per_second"),
                intProperty(properties, prefix + "cost_gold"),
                required(properties, prefix + "attack_mode"),
                optionalDoubleProperty(properties, prefix + "sell_refund_ratio", 0.5)
            );
            definitionsById.put(id, definition);
        }

        return new TowerCatalog(definitionsById);
    }

    private double doubleProperty(Properties properties, String key) {
        return Double.parseDouble(required(properties, key));
    }

    private int intProperty(Properties properties, String key) {
        return Integer.parseInt(required(properties, key));
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required tower property: " + key);
        }
        return value;
    }

    private double optionalDoubleProperty(Properties properties, String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Double.parseDouble(value);
    }
}
