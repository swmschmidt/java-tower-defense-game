package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.wave.WaveCatalog;
import com.swmschmidt.td.core.gameplay.wave.WaveDefinition;
import com.swmschmidt.td.core.gameplay.wave.WaveSpawnDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class WaveContentLoader {

    public WaveCatalog load(Path filePath) {
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(filePath)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load wave content from " + filePath, exception);
        }

        String[] waveIds = required(properties, "wave.ids").split(";");
        List<WaveDefinition> waves = new ArrayList<>();
        for (String waveIdToken : waveIds) {
            String waveId = waveIdToken.trim();
            if (waveId.isBlank()) {
                continue;
            }
            waves.add(loadWave(properties, waveId));
        }

        return new WaveCatalog(waves);
    }

    private WaveDefinition loadWave(Properties properties, String waveId) {
        String[] spawnIds = required(properties, "wave." + waveId + ".spawn.ids").split(";");
        List<WaveSpawnDefinition> spawns = new ArrayList<>();

        for (String spawnIdToken : spawnIds) {
            String spawnId = spawnIdToken.trim();
            if (spawnId.isBlank()) {
                continue;
            }

            String prefix = "wave." + waveId + ".spawn." + spawnId + ".";
            WaveSpawnDefinition spawn = new WaveSpawnDefinition(
                required(properties, prefix + "enemy_id"),
                intProperty(properties, prefix + "count"),
                doubleProperty(properties, prefix + "interval_seconds")
            );
            spawns.add(spawn);
        }

        return new WaveDefinition(waveId, spawns);
    }

    private int intProperty(Properties properties, String key) {
        return Integer.parseInt(required(properties, key));
    }

    private double doubleProperty(Properties properties, String key) {
        return Double.parseDouble(required(properties, key));
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required wave property: " + key);
        }
        return value;
    }
}
