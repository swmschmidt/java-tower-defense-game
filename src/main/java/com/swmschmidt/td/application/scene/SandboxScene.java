package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyInstance;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.MapDebugView;
import com.swmschmidt.td.core.scene.EnemyView;
import com.swmschmidt.td.core.scene.Scene;
import com.swmschmidt.td.core.scene.WorldView;

import java.util.ArrayList;
import java.util.List;

public final class SandboxScene implements Scene {
    private final GridDefinition grid;
    private final GameplayMap gameplayMap;
    private final EnemyCatalog enemyCatalog;
    private final String spawnEnemyId;
    private final double spawnIntervalSeconds;
    private final int spawnMaxCount;

    private final List<EnemyInstance> activeEnemies;
    private double spawnAccumulatorSeconds;
    private int spawnedCount;
    private boolean defeatTriggered;

    public SandboxScene(
        GridDefinition grid,
        GameplayMap gameplayMap,
        EnemyCatalog enemyCatalog,
        String spawnEnemyId,
        double spawnIntervalSeconds,
        int spawnMaxCount
    ) {
        this.grid = grid;
        this.gameplayMap = gameplayMap;
        this.enemyCatalog = enemyCatalog;
        this.spawnEnemyId = spawnEnemyId;
        this.spawnIntervalSeconds = spawnIntervalSeconds;
        this.spawnMaxCount = spawnMaxCount;

        this.activeEnemies = new ArrayList<>();
        this.spawnAccumulatorSeconds = 0.0;
        this.spawnedCount = 0;
        this.defeatTriggered = false;

        if (spawnIntervalSeconds <= 0.0) {
            throw new IllegalArgumentException("spawnIntervalSeconds must be positive");
        }
        if (spawnMaxCount < 1) {
            throw new IllegalArgumentException("spawnMaxCount must be at least 1");
        }
    }

    @Override
    public void update(double deltaSeconds) {
        if (defeatTriggered) {
            return;
        }

        spawnAccumulatorSeconds += deltaSeconds;
        while (spawnedCount < spawnMaxCount && spawnAccumulatorSeconds >= spawnIntervalSeconds) {
            spawnAccumulatorSeconds -= spawnIntervalSeconds;
            activeEnemies.add(new EnemyInstance(enemyCatalog.required(spawnEnemyId), gameplayMap.path()));
            spawnedCount++;
        }

        for (EnemyInstance enemy : activeEnemies) {
            enemy.update(deltaSeconds, gameplayMap.path());
            if (enemy.reachedGoal()) {
                defeatTriggered = true;
            }
        }
    }

    @Override
    public WorldView captureView() {
        List<EnemyView> enemyViews = activeEnemies.stream()
            .map(enemy -> new EnemyView(
                enemy.definition().id(),
                enemy.position(),
                enemy.definition().radius()
            ))
            .toList();

        MapDebugView mapDebugView = new MapDebugView(
            gameplayMap.path(),
            gameplayMap.buildableCells(),
            gameplayMap.blockedCells()
        );

        return new WorldView(grid, mapDebugView, enemyViews, defeatTriggered);
    }
}
