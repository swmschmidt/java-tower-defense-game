package com.swmschmidt.td.core.gameplay.wave;

import java.util.List;

public record WaveCatalog(List<WaveDefinition> waves) {
    public WaveCatalog {
        if (waves == null || waves.isEmpty()) {
            throw new IllegalArgumentException("At least one wave is required");
        }
        waves = List.copyOf(waves);
    }

    public int totalWaves() {
        return waves.size();
    }

    public WaveDefinition waveAt(int index) {
        if (index < 0 || index >= waves.size()) {
            throw new IllegalArgumentException("Wave index out of bounds: " + index);
        }
        return waves.get(index);
    }
}
