package com.swmschmidt.td.core.scene;

import com.swmschmidt.td.core.gameplay.map.GridCell;
import com.swmschmidt.td.core.gameplay.map.MapPath;

import java.util.Set;

public record MapDebugView(
    MapPath path,
    Set<GridCell> buildableCells,
    Set<GridCell> blockedCells
) {
    public MapDebugView {
        buildableCells = Set.copyOf(buildableCells);
        blockedCells = Set.copyOf(blockedCells);
    }
}
