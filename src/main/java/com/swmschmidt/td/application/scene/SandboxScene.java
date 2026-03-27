package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyInstance;
import com.swmschmidt.td.core.gameplay.combat.HitscanAttackResolver;
import com.swmschmidt.td.core.gameplay.combat.TowerCombatSystem;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.map.GridCell;
import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.tower.TowerDefinition;
import com.swmschmidt.td.core.gameplay.tower.TowerInstance;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.MapDebugView;
import com.swmschmidt.td.core.scene.EnemyView;
import com.swmschmidt.td.core.scene.Scene;
import com.swmschmidt.td.core.scene.TowerView;
import com.swmschmidt.td.core.scene.WorldView;
import com.swmschmidt.td.core.math.Vector3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

public final class SandboxScene implements Scene {
    private final GridDefinition grid;
    private final GameplayMap gameplayMap;
    private final EnemyCatalog enemyCatalog;
    private final TowerCatalog towerCatalog;
    private final String spawnEnemyId;
    private final double spawnIntervalSeconds;
    private final int spawnMaxCount;
    private final String defaultTowerId;
    private final BooleanSupplier placeTowerRequested;
    private final TowerCombatSystem towerCombatSystem;

    private final List<EnemyInstance> activeEnemies;
    private final List<TowerInstance> activeTowers;
    private final Set<GridCell> occupiedTowerCells;
    private double spawnAccumulatorSeconds;
    private int spawnedCount;
    private int playerGold;
    private int playerLives;
    private boolean defeatTriggered;

    public SandboxScene(
        GridDefinition grid,
        GameplayMap gameplayMap,
        EnemyCatalog enemyCatalog,
        TowerCatalog towerCatalog,
        String spawnEnemyId,
        double spawnIntervalSeconds,
        int spawnMaxCount,
        String defaultTowerId,
        int startingGold,
        int startingLives,
        BooleanSupplier placeTowerRequested
    ) {
        this.grid = grid;
        this.gameplayMap = gameplayMap;
        this.enemyCatalog = enemyCatalog;
        this.towerCatalog = towerCatalog;
        this.spawnEnemyId = spawnEnemyId;
        this.spawnIntervalSeconds = spawnIntervalSeconds;
        this.spawnMaxCount = spawnMaxCount;
        this.defaultTowerId = defaultTowerId;
        this.placeTowerRequested = placeTowerRequested;

        this.activeEnemies = new ArrayList<>();
        this.activeTowers = new ArrayList<>();
        this.occupiedTowerCells = new LinkedHashSet<>();
        this.spawnAccumulatorSeconds = 0.0;
        this.spawnedCount = 0;
        this.playerGold = startingGold;
        this.playerLives = startingLives;
        this.defeatTriggered = false;
        this.towerCombatSystem = new TowerCombatSystem(List.of(new HitscanAttackResolver()));

        if (spawnIntervalSeconds <= 0.0) {
            throw new IllegalArgumentException("spawnIntervalSeconds must be positive");
        }
        if (spawnMaxCount < 1) {
            throw new IllegalArgumentException("spawnMaxCount must be at least 1");
        }
        if (startingGold < 0) {
            throw new IllegalArgumentException("startingGold must be at least zero");
        }
        if (startingLives < 1) {
            throw new IllegalArgumentException("startingLives must be at least 1");
        }
    }

    @Override
    public void update(double deltaSeconds) {
        if (defeatTriggered) {
            return;
        }

        if (placeTowerRequested.getAsBoolean()) {
            placeDefaultTowerAtNextCell();
        }

        spawnAccumulatorSeconds += deltaSeconds;
        while (spawnedCount < spawnMaxCount && spawnAccumulatorSeconds >= spawnIntervalSeconds) {
            spawnAccumulatorSeconds -= spawnIntervalSeconds;
            activeEnemies.add(new EnemyInstance(enemyCatalog.required(spawnEnemyId), gameplayMap.path()));
            spawnedCount++;
        }

        for (EnemyInstance enemy : activeEnemies) {
            enemy.update(deltaSeconds, gameplayMap.path());
        }

        towerCombatSystem.update(activeTowers, activeEnemies, deltaSeconds);

        List<EnemyInstance> survivors = new ArrayList<>(activeEnemies.size());
        for (EnemyInstance enemy : activeEnemies) {
            if (!enemy.isAlive()) {
                playerGold += enemy.definition().goldReward();
                continue;
            }
            if (enemy.reachedGoal()) {
                playerLives--;
                if (playerLives <= 0) {
                    defeatTriggered = true;
                }
                continue;
            }
            survivors.add(enemy);
        }
        activeEnemies.clear();
        activeEnemies.addAll(survivors);
    }

    @Override
    public WorldView captureView() {
        List<TowerView> towerViews = activeTowers.stream()
            .map(tower -> new TowerView(
                tower.definition().id(),
                tower.position(),
                tower.definition().rangeUnits()
            ))
            .toList();

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

        return new WorldView(grid, mapDebugView, towerViews, enemyViews, playerGold, playerLives, defeatTriggered);
    }

    private void placeDefaultTowerAtNextCell() {
        TowerDefinition tower = towerCatalog.required(defaultTowerId);
        if (playerGold < tower.costGold()) {
            return;
        }

        GridCell nextCell = gameplayMap.buildableCells().stream()
            .filter(cell -> !gameplayMap.blockedCells().contains(cell))
            .filter(cell -> !occupiedTowerCells.contains(cell))
            .sorted(Comparator.comparingInt(GridCell::x).thenComparingInt(GridCell::z))
            .findFirst()
            .orElse(null);
        if (nextCell == null) {
            return;
        }

        Vector3 position = new Vector3(
            nextCell.x() * grid.cellSize(),
            0.0,
            nextCell.z() * grid.cellSize()
        );

        activeTowers.add(new TowerInstance(tower, position));
        occupiedTowerCells.add(nextCell);
        playerGold -= tower.costGold();
    }
}
