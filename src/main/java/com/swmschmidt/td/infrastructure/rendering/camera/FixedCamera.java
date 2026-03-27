package com.swmschmidt.td.infrastructure.rendering.camera;

import com.swmschmidt.td.core.math.Vector3;

public record FixedCamera(Vector3 position, Vector3 target, double fovDegrees) {
}
