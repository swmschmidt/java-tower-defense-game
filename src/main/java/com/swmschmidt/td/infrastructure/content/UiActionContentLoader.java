package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.uiaction.UiActionCatalog;
import com.swmschmidt.td.core.gameplay.uiaction.UiActionDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public final class UiActionContentLoader {

    public UiActionCatalog load(Path filePath) {
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(filePath)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load UI action content from " + filePath, exception);
        }

        String[] ids = required(properties, "ui.action.ids").split(";");
        List<UiActionDefinition> definitions = new ArrayList<>();
        for (String rawId : ids) {
            String id = rawId.trim();
            if (id.isBlank()) {
                continue;
            }
            String prefix = "ui.action." + id + ".";
            String[] rawEntityTypes = required(properties, prefix + "entity_types").split(";");
            Set<String> supportedEntityTypes = new LinkedHashSet<>();
            for (String value : rawEntityTypes) {
                if (!value.isBlank()) {
                    supportedEntityTypes.add(value.trim());
                }
            }
            definitions.add(new UiActionDefinition(
                id,
                required(properties, prefix + "label"),
                required(properties, prefix + "hotkey"),
                supportedEntityTypes
            ));
        }

        return new UiActionCatalog(definitions);
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required UI action property: " + key);
        }
        return value;
    }
}
