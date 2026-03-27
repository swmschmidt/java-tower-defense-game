package com.swmschmidt.td.infrastructure.config;

import com.swmschmidt.td.core.math.Vector3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfigLoader {
    private static final String CLASSPATH_LOCATION = "/config/app.properties";
    private static final Path EXTERNAL_LOCATION = Path.of("config", "app.properties");

    public AppConfig load() {
        Properties properties = new Properties();
        loadClasspathDefaults(properties);
        overrideFromExternalConfig(properties);
        return toConfig(properties);
    }

    private void loadClasspathDefaults(Properties properties) {
        try (InputStream stream = AppConfigLoader.class.getResourceAsStream(CLASSPATH_LOCATION)) {
            if (stream == null) {
                throw new IllegalStateException("Missing default config at " + CLASSPATH_LOCATION);
            }
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load default config", exception);
        }
    }

    private void overrideFromExternalConfig(Properties properties) {
        if (!Files.exists(EXTERNAL_LOCATION)) {
            return;
        }
        try (InputStream stream = Files.newInputStream(EXTERNAL_LOCATION)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load external config from " + EXTERNAL_LOCATION, exception);
        }
    }

    private AppConfig toConfig(Properties properties) {
        return new AppConfig(
            properties.getProperty("app.title"),
            intProperty(properties, "window.width"),
            intProperty(properties, "window.height"),
            intProperty(properties, "loop.updates_per_second"),
            intProperty(properties, "loop.frames_per_second"),
            vectorProperty(properties, "camera.position"),
            vectorProperty(properties, "camera.target"),
            doubleProperty(properties, "camera.fov_degrees"),
            intProperty(properties, "grid.half_size"),
            doubleProperty(properties, "grid.cell_size"),
            required(properties, "content.map_file"),
            required(properties, "content.enemies_file"),
            required(properties, "content.towers_file"),
            required(properties, "content.builders_file"),
            required(properties, "content.ui_actions_file"),
            required(properties, "content.waves_file"),
            doubleProperty(properties, "gameplay.pre_wave_delay_seconds"),
            doubleProperty(properties, "gameplay.post_wave_delay_seconds"),
            required(properties, "gameplay.default_tower_id"),
            required(properties, "gameplay.default_builder_id"),
            required(properties, "gameplay.default_hud_action_id"),
            intProperty(properties, "gameplay.starting_gold"),
            intProperty(properties, "gameplay.starting_lives")
        );
    }

    private int intProperty(Properties properties, String key) {
        return Integer.parseInt(required(properties, key));
    }

    private double doubleProperty(Properties properties, String key) {
        return Double.parseDouble(required(properties, key));
    }

    private Vector3 vectorProperty(Properties properties, String keyPrefix) {
        return new Vector3(
            doubleProperty(properties, keyPrefix + ".x"),
            doubleProperty(properties, keyPrefix + ".y"),
            doubleProperty(properties, keyPrefix + ".z")
        );
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config property: " + key);
        }
        return value;
    }
}
