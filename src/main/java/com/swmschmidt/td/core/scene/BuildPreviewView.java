package com.swmschmidt.td.core.scene;

import com.swmschmidt.td.core.math.Vector3;

public record BuildPreviewView(
    String towerId,
    Vector3 position,
    boolean valid
) {
}
