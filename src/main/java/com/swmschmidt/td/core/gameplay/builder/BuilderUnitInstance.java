package com.swmschmidt.td.core.gameplay.builder;

import com.swmschmidt.td.core.math.Vector3;

public final class BuilderUnitInstance {
    private final String instanceId;
    private final BuilderDefinition definition;
    private Vector3 position;
    private Vector3 movementTarget;

    public BuilderUnitInstance(String instanceId, BuilderDefinition definition, Vector3 position) {
        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("Builder instanceId must not be blank");
        }
        this.instanceId = instanceId;
        this.definition = definition;
        this.position = position;
    }

    public String instanceId() {
        return instanceId;
    }

    public BuilderDefinition definition() {
        return definition;
    }

    public Vector3 position() {
        return position;
    }

    public Vector3 movementTarget() {
        return movementTarget;
    }

    public void setMovementTarget(Vector3 movementTarget) {
        this.movementTarget = movementTarget;
    }

    public void update(double deltaSeconds) {
        if (movementTarget == null) {
            return;
        }

        Vector3 toTarget = movementTarget.subtract(position);
        double distance = toTarget.length();
        if (distance <= 1e-4) {
            position = movementTarget;
            movementTarget = null;
            return;
        }

        double maxStep = definition.moveSpeedUnitsPerSecond() * deltaSeconds;
        if (maxStep >= distance) {
            position = movementTarget;
            movementTarget = null;
            return;
        }

        Vector3 direction = toTarget.multiply(1.0 / distance);
        position = position.add(direction.multiply(maxStep));
    }
}
