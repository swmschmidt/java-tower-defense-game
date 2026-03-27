package com.swmschmidt.td.core.gameplay.wave;

import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyInstance;
import com.swmschmidt.td.core.gameplay.map.MapPath;

import java.util.ArrayList;
import java.util.List;

public final class WaveSpawnerService {
    private final WaveDefinition wave;
    private int spawnIndex;
    private int remainingInCurrentSpawn;
    private double spawnAccumulatorSeconds;

    public WaveSpawnerService(WaveDefinition wave) {
        this.wave = wave;
        this.spawnIndex = 0;
        this.remainingInCurrentSpawn = wave.spawns().getFirst().count();
        this.spawnAccumulatorSeconds = 0.0;
    }

    public List<EnemyInstance> update(double deltaSeconds, EnemyCatalog enemyCatalog, MapPath path) {
        List<EnemyInstance> spawnedEnemies = new ArrayList<>();
        if (isCompleted()) {
            return spawnedEnemies;
        }

        spawnAccumulatorSeconds += deltaSeconds;
        while (!isCompleted()) {
            WaveSpawnDefinition spawn = currentSpawn();
            if (spawnAccumulatorSeconds < spawn.intervalSeconds()) {
                break;
            }

            spawnAccumulatorSeconds -= spawn.intervalSeconds();
            spawnedEnemies.add(new EnemyInstance(enemyCatalog.required(spawn.enemyId()), path));
            remainingInCurrentSpawn--;
            if (remainingInCurrentSpawn == 0) {
                moveToNextSpawn();
            }
        }

        return spawnedEnemies;
    }

    public boolean isCompleted() {
        return spawnIndex >= wave.spawns().size();
    }

    private WaveSpawnDefinition currentSpawn() {
        return wave.spawns().get(spawnIndex);
    }

    private void moveToNextSpawn() {
        spawnIndex++;
        if (!isCompleted()) {
            remainingInCurrentSpawn = currentSpawn().count();
        }
    }
}
