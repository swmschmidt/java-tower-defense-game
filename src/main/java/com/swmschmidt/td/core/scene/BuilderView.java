package com.swmschmidt.td.core.scene;

import com.swmschmidt.td.core.math.Vector3;

public record BuilderView(
    String instanceId,
    String definitionId,
    Vector3 position,
    double selectionRadius,
    boolean selected
) {
}
