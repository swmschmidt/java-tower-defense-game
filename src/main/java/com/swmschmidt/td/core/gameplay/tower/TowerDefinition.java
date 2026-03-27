package com.swmschmidt.td.core.gameplay.tower;

public record TowerDefinition(
    String id,
    double rangeUnits,
    double damagePerShot,
    double attacksPerSecond,
    int costGold,
    String attackMode
) {
    public TowerDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Tower id must not be blank");
        }
        if (rangeUnits <= 0.0) {
            throw new IllegalArgumentException("Tower range must be positive");
        }
        if (damagePerShot <= 0.0) {
            throw new IllegalArgumentException("Tower damagePerShot must be positive");
        }
        if (attacksPerSecond <= 0.0) {
            throw new IllegalArgumentException("Tower attacksPerSecond must be positive");
        }
        if (costGold < 0) {
            throw new IllegalArgumentException("Tower costGold must be at least zero");
        }
        if (attackMode == null || attackMode.isBlank()) {
            throw new IllegalArgumentException("Tower attackMode must not be blank");
        }
    }
}
