package com.swmschmidt.td.core.gameplay.enemy;

import com.swmschmidt.td.core.gameplay.map.MapPath;
import com.swmschmidt.td.core.math.Vector3;

public final class EnemyInstance {
    private final EnemyDefinition definition;
    private double distanceAlongPath;
    private Vector3 position;
    private boolean reachedGoal;
    private double health;
    private boolean alive;

    public EnemyInstance(EnemyDefinition definition, MapPath path) {
        this.definition = definition;
        this.distanceAlongPath = 0.0;
        this.position = path.start();
        this.reachedGoal = false;
        this.health = definition.maxHealth();
        this.alive = true;
    }

    public void update(double deltaSeconds, MapPath path) {
        if (reachedGoal || !alive) {
            return;
        }

        distanceAlongPath += definition.speedUnitsPerSecond() * deltaSeconds;
        if (distanceAlongPath >= path.totalLength()) {
            distanceAlongPath = path.totalLength();
            reachedGoal = true;
        }
        position = path.sample(distanceAlongPath);
    }

    public EnemyDefinition definition() {
        return definition;
    }

    public Vector3 position() {
        return position;
    }

    public boolean reachedGoal() {
        return reachedGoal;
    }

    public double health() {
        return health;
    }

    public boolean isAlive() {
        return alive;
    }

    public double healthRatio() {
        return health / definition.maxHealth();
    }

    public void applyDamage(double amount) {
        if (!alive || amount <= 0.0) {
            return;
        }
        health = Math.max(0.0, health - amount);
        if (health == 0.0) {
            alive = false;
        }
    }
}
