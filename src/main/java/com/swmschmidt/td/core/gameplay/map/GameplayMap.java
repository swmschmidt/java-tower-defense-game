package com.swmschmidt.td.core.gameplay.map;

import java.util.Set;

public record GameplayMap(
    String id,
    MapPath path,
    Set<GridCell> buildableCells,
    Set<GridCell> blockedCells
) {
    public GameplayMap {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Map id must not be blank");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        buildableCells = Set.copyOf(buildableCells);
        blockedCells = Set.copyOf(blockedCells);
    }
}
