package com.swmschmidt.td.infrastructure.rendering.camera;

import com.swmschmidt.td.core.math.Vector3;

import java.util.Optional;

public final class ScreenToWorldRayPicker {
    public Optional<Vector3> pickGround(
        int screenX,
        int screenY,
        int viewportWidth,
        int viewportHeight,
        FixedCamera camera
    ) {
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            return Optional.empty();
        }

        Vector3 forward = camera.target().subtract(camera.position()).normalize();
        Vector3 right = forward.cross(Vector3.UP).normalize();
        Vector3 up = right.cross(forward).normalize();

        double fovRadians = Math.toRadians(camera.fovDegrees());
        double tanHalfFov = Math.tan(fovRadians * 0.5);
        double aspect = (double) viewportWidth / (double) viewportHeight;

        double ndcX = ((screenX + 0.5) / viewportWidth) * 2.0 - 1.0;
        double ndcY = 1.0 - ((screenY + 0.5) / viewportHeight) * 2.0;

        Vector3 rayDirection = forward
            .add(right.multiply(ndcX * aspect * tanHalfFov))
            .add(up.multiply(ndcY * tanHalfFov))
            .normalize();

        double denominator = rayDirection.y();
        if (Math.abs(denominator) < 1e-8) {
            return Optional.empty();
        }

        double t = -camera.position().y() / denominator;
        if (t < 0.0) {
            return Optional.empty();
        }

        return Optional.of(camera.position().add(rayDirection.multiply(t)));
    }
}
