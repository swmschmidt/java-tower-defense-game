package com.swmschmidt.td.core.gameplay.enemy;

import com.swmschmidt.td.core.gameplay.map.MapPath;
import com.swmschmidt.td.core.math.Vector3;

public final class EnemyInstance {
    private final EnemyDefinition definition;
    private double distanceAlongPath;
    private Vector3 position;
    private boolean reachedGoal;

    public EnemyInstance(EnemyDefinition definition, MapPath path) {
        this.definition = definition;
        this.distanceAlongPath = 0.0;
        this.position = path.start();
        this.reachedGoal = false;
    }

    public void update(double deltaSeconds, MapPath path) {
        if (reachedGoal) {
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
}
