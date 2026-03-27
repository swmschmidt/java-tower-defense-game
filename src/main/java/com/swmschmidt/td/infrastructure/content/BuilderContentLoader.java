package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.builder.BuilderCatalog;
import com.swmschmidt.td.core.gameplay.builder.BuilderDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class BuilderContentLoader {

    public BuilderCatalog load(Path filePath) {
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(filePath)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load builder content from " + filePath, exception);
        }

        String[] ids = required(properties, "builder.ids").split(";");
        Map<String, BuilderDefinition> definitionsById = new LinkedHashMap<>();
        for (String rawId : ids) {
            String id = rawId.trim();
            if (id.isBlank()) {
                continue;
            }
            String prefix = "builder." + id + ".";
            BuilderDefinition definition = new BuilderDefinition(
                id,
                Double.parseDouble(required(properties, prefix + "move_speed_units_per_second")),
                Double.parseDouble(required(properties, prefix + "selection_radius")),
                Double.parseDouble(required(properties, prefix + "build_range_units")),
                Double.parseDouble(required(properties, prefix + "spawn_x")),
                Double.parseDouble(required(properties, prefix + "spawn_z"))
            );
            definitionsById.put(definition.id(), definition);
        }

        return new BuilderCatalog(definitionsById);
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required builder property: " + key);
        }
        return value;
    }
}
