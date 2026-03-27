package com.swmschmidt.td.core.gameplay.wave;

public record WaveSpawnDefinition(
    String enemyId,
    int count,
    double intervalSeconds
) {
    public WaveSpawnDefinition {
        if (enemyId == null || enemyId.isBlank()) {
            throw new IllegalArgumentException("enemyId must not be blank");
        }
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least 1");
        }
        if (intervalSeconds <= 0.0) {
            throw new IllegalArgumentException("intervalSeconds must be positive");
        }
    }
}
