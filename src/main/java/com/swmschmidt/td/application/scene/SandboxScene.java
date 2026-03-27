package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.Scene;
import com.swmschmidt.td.core.scene.WorldView;

public final class SandboxScene implements Scene {
    private final GridDefinition grid;

    public SandboxScene(GridDefinition grid) {
        this.grid = grid;
    }

    @Override
    public void update(double deltaSeconds) {
        // Step 01 intentionally has no gameplay simulation yet.
    }

    @Override
    public WorldView captureView() {
        return new WorldView(grid);
    }
}
