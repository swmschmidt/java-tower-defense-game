package com.swmschmidt.td.core.gameplay.enemy;

public record EnemyDefinition(
    String id,
    double speedUnitsPerSecond,
    double radius,
    double maxHealth,
    int goldReward
) {
    public EnemyDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Enemy id must not be blank");
        }
        if (speedUnitsPerSecond <= 0.0) {
            throw new IllegalArgumentException("Enemy speed must be positive");
        }
        if (radius <= 0.0) {
            throw new IllegalArgumentException("Enemy radius must be positive");
        }
        if (maxHealth <= 0.0) {
            throw new IllegalArgumentException("Enemy maxHealth must be positive");
        }
        if (goldReward < 0) {
            throw new IllegalArgumentException("Enemy goldReward must be at least zero");
        }
    }
}
