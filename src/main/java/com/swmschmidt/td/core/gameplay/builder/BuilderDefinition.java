package com.swmschmidt.td.core.gameplay.builder;

public record BuilderDefinition(
    String id,
    double moveSpeedUnitsPerSecond,
    double selectionRadius,
    double buildRangeUnits,
    double spawnX,
    double spawnZ
) {
    public BuilderDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Builder id must not be blank");
        }
        if (moveSpeedUnitsPerSecond <= 0.0) {
            throw new IllegalArgumentException("Builder moveSpeedUnitsPerSecond must be positive");
        }
        if (selectionRadius <= 0.0) {
            throw new IllegalArgumentException("Builder selectionRadius must be positive");
        }
        if (buildRangeUnits <= 0.0) {
            throw new IllegalArgumentException("Builder buildRangeUnits must be positive");
        }
    }
}
