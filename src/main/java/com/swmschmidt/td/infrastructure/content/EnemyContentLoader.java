package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class EnemyContentLoader {

    public EnemyCatalog load(Path filePath) {
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(filePath)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load enemy content from " + filePath, exception);
        }

        String[] ids = required(properties, "enemy.ids").split(";");
        Map<String, EnemyDefinition> definitionsById = new LinkedHashMap<>();

        for (String idToken : ids) {
            String id = idToken.trim();
            if (id.isBlank()) {
                continue;
            }

            String prefix = "enemy." + id + ".";
            EnemyDefinition definition = new EnemyDefinition(
                id,
                doubleProperty(properties, prefix + "speed_units_per_second"),
                doubleProperty(properties, prefix + "radius")
            );
            definitionsById.put(id, definition);
        }

        return new EnemyCatalog(definitionsById);
    }

    private double doubleProperty(Properties properties, String key) {
        return Double.parseDouble(required(properties, key));
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required enemy property: " + key);
        }
        return value;
    }
}
