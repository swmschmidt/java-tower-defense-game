package com.swmschmidt.td.core.gameplay.wave;

import java.util.List;

public record WaveDefinition(
    String id,
    List<WaveSpawnDefinition> spawns
) {
    public WaveDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (spawns == null || spawns.isEmpty()) {
            throw new IllegalArgumentException("Wave must contain at least one spawn definition");
        }
        spawns = List.copyOf(spawns);
    }
}
