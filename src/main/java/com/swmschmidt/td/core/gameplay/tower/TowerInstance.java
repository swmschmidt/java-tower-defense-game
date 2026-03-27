package com.swmschmidt.td.core.gameplay.tower;

import com.swmschmidt.td.core.math.Vector3;

public final class TowerInstance {
    private final String instanceId;
    private final TowerDefinition definition;
    private final Vector3 position;
    private double cooldownSeconds;

    public TowerInstance(String instanceId, TowerDefinition definition, Vector3 position) {
        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("Tower instanceId must not be blank");
        }
        this.instanceId = instanceId;
        this.definition = definition;
        this.position = position;
        this.cooldownSeconds = 0.0;
    }

    public String instanceId() {
        return instanceId;
    }

    public TowerDefinition definition() {
        return definition;
    }

    public Vector3 position() {
        return position;
    }

    public void tickCooldown(double deltaSeconds) {
        cooldownSeconds = Math.max(0.0, cooldownSeconds - deltaSeconds);
    }

    public boolean isReadyToAttack() {
        return cooldownSeconds <= 0.0;
    }

    public void consumeAttack() {
        cooldownSeconds += 1.0 / definition.attacksPerSecond();
    }
}
