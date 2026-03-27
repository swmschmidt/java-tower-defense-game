package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.gameplay.builder.BuilderCatalog;
import com.swmschmidt.td.core.gameplay.builder.BuilderDefinition;
import com.swmschmidt.td.core.gameplay.builder.BuilderUnitInstance;
import com.swmschmidt.td.core.gameplay.command.GameCommand;
import com.swmschmidt.td.core.gameplay.command.MoveBuilderCommand;
import com.swmschmidt.td.core.gameplay.command.SelectEntityCommand;
import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyInstance;
import com.swmschmidt.td.core.gameplay.combat.HitscanAttackResolver;
import com.swmschmidt.td.core.gameplay.combat.TowerCombatSystem;
import com.swmschmidt.td.core.gameplay.match.MatchPhase;
import com.swmschmidt.td.core.gameplay.match.MatchStateMachine;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.map.GridCell;
import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.tower.TowerDefinition;
import com.swmschmidt.td.core.gameplay.tower.TowerInstance;
import com.swmschmidt.td.core.gameplay.wave.WaveCatalog;
import com.swmschmidt.td.core.gameplay.wave.WaveDefinition;
import com.swmschmidt.td.core.gameplay.wave.WaveSpawnerService;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.MapDebugView;
import com.swmschmidt.td.core.scene.BuilderView;
import com.swmschmidt.td.core.scene.EnemyView;
import com.swmschmidt.td.core.scene.Scene;
import com.swmschmidt.td.core.scene.TowerView;
import com.swmschmidt.td.core.scene.WorldView;
import com.swmschmidt.td.core.math.Vector3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class SandboxScene implements Scene {
    private final GridDefinition grid;
    private final GameplayMap gameplayMap;
    private final EnemyCatalog enemyCatalog;
    private final TowerCatalog towerCatalog;
    private final BuilderCatalog builderCatalog;
    private final WaveCatalog waveCatalog;
    private final String defaultTowerId;
    private final String defaultBuilderId;
    private final BooleanSupplier placeTowerRequested;
    private final Supplier<Optional<Vector3>> selectWorldPointRequested;
    private final Supplier<Optional<Vector3>> contextWorldPointRequested;
    private final TowerCombatSystem towerCombatSystem;
    private final MatchStateMachine matchStateMachine;
    private final Queue<GameCommand> pendingCommands;
    private final BuilderUnitInstance builderUnit;

    private final List<EnemyInstance> activeEnemies;
    private final List<TowerInstance> activeTowers;
    private final Set<GridCell> occupiedTowerCells;
    private WaveSpawnerService waveSpawner;
    private String selectedEntityType;
    private String selectedEntityId;
    private long simulationTick;
    private int playerGold;
    private int playerLives;

    public SandboxScene(
        GridDefinition grid,
        GameplayMap gameplayMap,
        EnemyCatalog enemyCatalog,
        TowerCatalog towerCatalog,
        BuilderCatalog builderCatalog,
        WaveCatalog waveCatalog,
        double preWaveDelaySeconds,
        double postWaveDelaySeconds,
        String defaultTowerId,
        String defaultBuilderId,
        int startingGold,
        int startingLives,
        BooleanSupplier placeTowerRequested,
        Supplier<Optional<Vector3>> selectWorldPointRequested,
        Supplier<Optional<Vector3>> contextWorldPointRequested
    ) {
        this.grid = grid;
        this.gameplayMap = gameplayMap;
        this.enemyCatalog = enemyCatalog;
        this.towerCatalog = towerCatalog;
        this.builderCatalog = builderCatalog;
        this.waveCatalog = waveCatalog;
        this.defaultTowerId = defaultTowerId;
        this.defaultBuilderId = defaultBuilderId;
        this.placeTowerRequested = placeTowerRequested;
        this.selectWorldPointRequested = selectWorldPointRequested;
        this.contextWorldPointRequested = contextWorldPointRequested;

        this.activeEnemies = new ArrayList<>();
        this.activeTowers = new ArrayList<>();
        this.occupiedTowerCells = new LinkedHashSet<>();
        this.pendingCommands = new ArrayDeque<>();
        this.builderUnit = createDefaultBuilderInstance();
        this.selectedEntityType = "";
        this.selectedEntityId = "";
        this.simulationTick = 0L;
        this.playerGold = startingGold;
        this.playerLives = startingLives;
        this.towerCombatSystem = new TowerCombatSystem(List.of(new HitscanAttackResolver()));
        this.matchStateMachine = new MatchStateMachine(
            waveCatalog.totalWaves(),
            preWaveDelaySeconds,
            postWaveDelaySeconds
        );
        this.waveSpawner = null;

        if (startingGold < 0) {
            throw new IllegalArgumentException("startingGold must be at least zero");
        }
        if (startingLives < 1) {
            throw new IllegalArgumentException("startingLives must be at least 1");
        }
    }

    @Override
    public void update(double deltaSeconds) {
        if (matchStateMachine.isTerminal()) {
            return;
        }
        simulationTick++;

        enqueueInputCommands();
        processPendingCommands();

        if (placeTowerRequested.getAsBoolean()) {
            placeDefaultTowerAtNextCell();
        }

        updateMatchFlow(deltaSeconds);

        for (EnemyInstance enemy : activeEnemies) {
            enemy.update(deltaSeconds, gameplayMap.path());
        }

        builderUnit.update(deltaSeconds);
        towerCombatSystem.update(activeTowers, activeEnemies, deltaSeconds);
        removeResolvedEnemiesAndApplyEconomy();
    }

    private BuilderUnitInstance createDefaultBuilderInstance() {
        BuilderDefinition definition = builderCatalog.required(defaultBuilderId);
        return new BuilderUnitInstance(
            "builder-1",
            definition,
            new Vector3(definition.spawnX(), 0.0, definition.spawnZ())
        );
    }

    private void enqueueInputCommands() {
        selectWorldPointRequested.get().ifPresent(worldPoint -> {
            PickedEntity picked = pickEntityAt(worldPoint);
            pendingCommands.add(new SelectEntityCommand(
                "local",
                simulationTick,
                picked == null ? "" : picked.type,
                picked == null ? "" : picked.id
            ));
        });

        contextWorldPointRequested.get().ifPresent(worldPoint -> {
            if ("builder".equals(selectedEntityType) && builderUnit.instanceId().equals(selectedEntityId)) {
                pendingCommands.add(new MoveBuilderCommand(
                    "local",
                    simulationTick,
                    builderUnit.instanceId(),
                    new Vector3(worldPoint.x(), 0.0, worldPoint.z())
                ));
            }
        });
    }

    private void processPendingCommands() {
        GameCommand command;
        while ((command = pendingCommands.poll()) != null) {
            if (command instanceof SelectEntityCommand selectEntityCommand) {
                selectedEntityType = selectEntityCommand.entityType();
                selectedEntityId = selectEntityCommand.entityId();
            } else if (command instanceof MoveBuilderCommand moveBuilderCommand) {
                applyMoveBuilderCommand(moveBuilderCommand);
            }
        }
    }

    private void applyMoveBuilderCommand(MoveBuilderCommand command) {
        if (!builderUnit.instanceId().equals(command.builderInstanceId())) {
            return;
        }
        builderUnit.setMovementTarget(command.targetPosition());
    }

    private PickedEntity pickEntityAt(Vector3 worldPoint) {
        double builderDistanceSq = horizontalDistanceSquared(builderUnit.position(), worldPoint);
        double builderPickRadius = Math.max(builderUnit.definition().selectionRadius(), 0.45 * grid.cellSize());
        if (builderDistanceSq <= builderPickRadius * builderPickRadius) {
            return new PickedEntity("builder", builderUnit.instanceId());
        }

        double towerPickRadius = 0.45 * grid.cellSize();
        for (TowerInstance tower : activeTowers) {
            if (horizontalDistanceSquared(tower.position(), worldPoint) <= towerPickRadius * towerPickRadius) {
                return new PickedEntity("tower", tower.definition().id());
            }
        }

        return null;
    }

    private double horizontalDistanceSquared(Vector3 a, Vector3 b) {
        double dx = a.x() - b.x();
        double dz = a.z() - b.z();
        return dx * dx + dz * dz;
    }

    private void updateMatchFlow(double deltaSeconds) {
        if (matchStateMachine.phase() == MatchPhase.PRE_WAVE) {
            matchStateMachine.updateBeforeWave(deltaSeconds);
            if (matchStateMachine.isInWave()) {
                startCurrentWave();
            }
            return;
        }

        if (matchStateMachine.isInWave()) {
            activeEnemies.addAll(waveSpawner.update(deltaSeconds, enemyCatalog, gameplayMap.path()));
            if (waveSpawner.isCompleted() && activeEnemies.isEmpty()) {
                matchStateMachine.onWaveCompleted();
            }
            return;
        }

        if (matchStateMachine.phase() == MatchPhase.POST_WAVE) {
            matchStateMachine.updateAfterWave(deltaSeconds);
            waveSpawner = null;
        }
    }

    private void startCurrentWave() {
        WaveDefinition wave = waveCatalog.waveAt(matchStateMachine.currentWaveNumber() - 1);
        waveSpawner = new WaveSpawnerService(wave);
    }

    private void removeResolvedEnemiesAndApplyEconomy() {
        List<EnemyInstance> survivors = new ArrayList<>(activeEnemies.size());
        for (EnemyInstance enemy : activeEnemies) {
            if (!enemy.isAlive()) {
                playerGold += enemy.definition().goldReward();
                continue;
            }
            if (enemy.reachedGoal()) {
                playerLives--;
                if (playerLives <= 0) {
                    matchStateMachine.onDefeat();
                }
                continue;
            }
            survivors.add(enemy);
        }
        activeEnemies.clear();
        activeEnemies.addAll(survivors);

        if (matchStateMachine.isInWave() && waveSpawner.isCompleted() && activeEnemies.isEmpty()) {
            matchStateMachine.onWaveCompleted();
        }

        if (matchStateMachine.phase() == MatchPhase.POST_WAVE) {
            matchStateMachine.updateAfterWave(0.0);
            if (matchStateMachine.phase() == MatchPhase.PRE_WAVE) {
                waveSpawner = null;
            }
        }
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

        List<BuilderView> builderViews = List.of(new BuilderView(
            builderUnit.instanceId(),
            builderUnit.definition().id(),
            builderUnit.position(),
            builderUnit.definition().selectionRadius(),
            "builder".equals(selectedEntityType) && builderUnit.instanceId().equals(selectedEntityId)
        ));

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

        return new WorldView(
            grid,
            mapDebugView,
            towerViews,
            builderViews,
            enemyViews,
            selectedEntityType,
            selectedEntityId,
            matchStateMachine.currentWaveNumber(),
            matchStateMachine.totalWaves(),
            matchStateMachine.phase().name(),
            playerGold,
            playerLives,
            matchStateMachine.phase() == MatchPhase.DEFEAT,
            matchStateMachine.phase() == MatchPhase.VICTORY
        );
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

    private record PickedEntity(String type, String id) {
    }
}
