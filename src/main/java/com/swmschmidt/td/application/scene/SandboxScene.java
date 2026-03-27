package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.gameplay.builder.BuilderCatalog;
import com.swmschmidt.td.core.gameplay.builder.BuilderDefinition;
import com.swmschmidt.td.core.gameplay.builder.BuilderUnitInstance;
import com.swmschmidt.td.core.gameplay.command.BuildTowerCommand;
import com.swmschmidt.td.core.gameplay.command.GameCommand;
import com.swmschmidt.td.core.gameplay.command.MoveBuilderCommand;
import com.swmschmidt.td.core.gameplay.command.SelectEntityCommand;
import com.swmschmidt.td.core.gameplay.command.SellTowerCommand;
import com.swmschmidt.td.core.gameplay.command.UiActionCommand;
import com.swmschmidt.td.core.gameplay.combat.HitscanAttackResolver;
import com.swmschmidt.td.core.gameplay.combat.TowerCombatSystem;
import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyInstance;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.map.GridCell;
import com.swmschmidt.td.core.gameplay.match.MatchPhase;
import com.swmschmidt.td.core.gameplay.match.MatchStateMachine;
import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.tower.TowerDefinition;
import com.swmschmidt.td.core.gameplay.tower.TowerInstance;
import com.swmschmidt.td.core.gameplay.uiaction.UiActionCatalog;
import com.swmschmidt.td.core.gameplay.uiaction.UiActionDefinition;
import com.swmschmidt.td.core.gameplay.uiaction.UiActionMode;
import com.swmschmidt.td.core.gameplay.wave.WaveCatalog;
import com.swmschmidt.td.core.gameplay.wave.WaveDefinition;
import com.swmschmidt.td.core.gameplay.wave.WaveSpawnerService;
import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.BuildPreviewView;
import com.swmschmidt.td.core.scene.BuilderView;
import com.swmschmidt.td.core.scene.EnemyView;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.HudActionView;
import com.swmschmidt.td.core.scene.MapDebugView;
import com.swmschmidt.td.core.scene.Scene;
import com.swmschmidt.td.core.scene.TowerView;
import com.swmschmidt.td.core.scene.WorldView;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
    private final UiActionCatalog uiActionCatalog;
    private final WaveCatalog waveCatalog;
    private final String defaultTowerId;
    private final String defaultBuilderId;
    private final String defaultHudActionId;
    private final BooleanSupplier placeTowerRequested;
    private final Supplier<Optional<Vector3>> selectWorldPointRequested;
    private final Supplier<Optional<Vector3>> contextWorldPointRequested;
    private final Supplier<Optional<String>> hudActionRequested;
    private final Supplier<Optional<String>> hudHotkeyActionRequested;
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
    private String activeHudActionId;
    private String actionFeedbackMessage;
    private BuildPreviewState buildPreviewState;
    private long simulationTick;
    private long towerSequence;
    private int playerGold;
    private int playerLives;

    public SandboxScene(
        GridDefinition grid,
        GameplayMap gameplayMap,
        EnemyCatalog enemyCatalog,
        TowerCatalog towerCatalog,
        BuilderCatalog builderCatalog,
        UiActionCatalog uiActionCatalog,
        WaveCatalog waveCatalog,
        double preWaveDelaySeconds,
        double postWaveDelaySeconds,
        String defaultTowerId,
        String defaultBuilderId,
        String defaultHudActionId,
        int startingGold,
        int startingLives,
        BooleanSupplier placeTowerRequested,
        Supplier<Optional<Vector3>> selectWorldPointRequested,
        Supplier<Optional<Vector3>> contextWorldPointRequested,
        Supplier<Optional<String>> hudActionRequested,
        Supplier<Optional<String>> hudHotkeyActionRequested
    ) {
        this.grid = grid;
        this.gameplayMap = gameplayMap;
        this.enemyCatalog = enemyCatalog;
        this.towerCatalog = towerCatalog;
        this.builderCatalog = builderCatalog;
        this.uiActionCatalog = uiActionCatalog;
        this.waveCatalog = waveCatalog;
        this.defaultTowerId = defaultTowerId;
        this.defaultBuilderId = defaultBuilderId;
        this.defaultHudActionId = defaultHudActionId;
        this.placeTowerRequested = placeTowerRequested;
        this.selectWorldPointRequested = selectWorldPointRequested;
        this.contextWorldPointRequested = contextWorldPointRequested;
        this.hudActionRequested = hudActionRequested;
        this.hudHotkeyActionRequested = hudHotkeyActionRequested;

        this.activeEnemies = new ArrayList<>();
        this.activeTowers = new ArrayList<>();
        this.occupiedTowerCells = new LinkedHashSet<>();
        this.pendingCommands = new ArrayDeque<>();
        this.builderUnit = createDefaultBuilderInstance();
        this.selectedEntityType = "";
        this.selectedEntityId = "";
        this.activeHudActionId = resolveFallbackActionId();
        this.actionFeedbackMessage = "";
        this.buildPreviewState = null;
        this.simulationTick = 0L;
        this.towerSequence = 0L;
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
        if (placeTowerRequested.getAsBoolean()) {
            pendingCommands.add(new BuildTowerCommand(
                "local",
                simulationTick,
                builderUnit.instanceId(),
                defaultTowerId,
                builderUnit.position()
            ));
        }
        processPendingCommands();

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
            UiActionDefinition activeAction = currentActiveAction();
            if (activeAction == null) {
                return;
            }
            if (activeAction.mode() == UiActionMode.MOVE && isSelectedBuilder()) {
                pendingCommands.add(new MoveBuilderCommand(
                    "local",
                    simulationTick,
                    builderUnit.instanceId(),
                    new Vector3(worldPoint.x(), 0.0, worldPoint.z())
                ));
                return;
            }
            if (activeAction.mode() == UiActionMode.BUILD && isSelectedBuilder()) {
                updateBuildPreview(activeAction.towerId(), worldPoint);
                pendingCommands.add(new BuildTowerCommand(
                    "local",
                    simulationTick,
                    builderUnit.instanceId(),
                    activeAction.towerId(),
                    worldPoint
                ));
                return;
            }
            if (activeAction.mode() == UiActionMode.SELL && "tower".equals(selectedEntityType)) {
                String towerInstanceId = resolveSellTargetTowerId(worldPoint);
                pendingCommands.add(new SellTowerCommand("local", simulationTick, towerInstanceId));
            }
        });

        hudActionRequested.get().ifPresent(actionId ->
            pendingCommands.add(new UiActionCommand("local", simulationTick, actionId))
        );

        hudHotkeyActionRequested.get().ifPresent(actionId ->
            pendingCommands.add(new UiActionCommand("local", simulationTick, actionId))
        );
    }

    private void processPendingCommands() {
        GameCommand command;
        while ((command = pendingCommands.poll()) != null) {
            if (command instanceof SelectEntityCommand selectEntityCommand) {
                selectedEntityType = selectEntityCommand.entityType();
                selectedEntityId = selectEntityCommand.entityId();
                activeHudActionId = resolveFallbackActionId();
                actionFeedbackMessage = "";
                if (!isBuildActionActive()) {
                    buildPreviewState = null;
                }
            } else if (command instanceof MoveBuilderCommand moveBuilderCommand) {
                applyMoveBuilderCommand(moveBuilderCommand);
            } else if (command instanceof BuildTowerCommand buildTowerCommand) {
                applyBuildTowerCommand(buildTowerCommand);
            } else if (command instanceof SellTowerCommand sellTowerCommand) {
                applySellTowerCommand(sellTowerCommand);
            } else if (command instanceof UiActionCommand uiActionCommand) {
                applyUiActionCommand(uiActionCommand);
            }
        }
    }

    private void applyUiActionCommand(UiActionCommand command) {
        if (!isActionAvailableForCurrentSelection(command.actionId())) {
            actionFeedbackMessage = "Action unavailable for the current selection";
            return;
        }
        activeHudActionId = command.actionId();
        UiActionDefinition definition = currentActiveAction();
        if (definition != null && definition.mode() == UiActionMode.CANCEL) {
            activeHudActionId = resolveFallbackActionId();
            buildPreviewState = null;
            actionFeedbackMessage = "Action canceled";
            return;
        }
        if (definition == null || definition.mode() != UiActionMode.BUILD) {
            buildPreviewState = null;
        }
        actionFeedbackMessage = "";
    }

    private boolean isActionAvailableForCurrentSelection(String actionId) {
        if (actionId == null || actionId.isBlank()) {
            return false;
        }
        String selectedType = selectedEntityType.isBlank() ? "none" : selectedEntityType;
        return uiActionCatalog.actionsForEntityType(selectedType).stream()
            .map(UiActionDefinition::id)
            .anyMatch(actionId::equals);
    }

    private String resolveFallbackActionId() {
        String selectedType = selectedEntityType.isBlank() ? "none" : selectedEntityType;
        List<UiActionDefinition> actions = uiActionCatalog.actionsForEntityType(selectedType);
        if (actions.isEmpty()) {
            return "";
        }
        if (isActionAvailableForCurrentSelection(defaultHudActionId)) {
            return defaultHudActionId;
        }
        for (UiActionDefinition action : actions) {
            if (action.mode() == UiActionMode.CANCEL) {
                return action.id();
            }
        }
        return actions.getFirst().id();
    }

    private void applyMoveBuilderCommand(MoveBuilderCommand command) {
        if (!builderUnit.instanceId().equals(command.builderInstanceId())) {
            return;
        }
        builderUnit.setMovementTarget(command.targetPosition());
        actionFeedbackMessage = "";
    }

    private void applyBuildTowerCommand(BuildTowerCommand command) {
        if (!builderUnit.instanceId().equals(command.builderInstanceId())) {
            return;
        }

        TowerDefinition tower = towerCatalog.required(command.towerDefinitionId());
        BuildValidationResult validation = validateBuildPlacement(tower, command.targetPosition());
        buildPreviewState = new BuildPreviewState(tower.id(), validation.position(), validation.valid());

        if (!validation.valid()) {
            actionFeedbackMessage = validation.reason();
            return;
        }

        TowerInstance placedTower = new TowerInstance(nextTowerInstanceId(), tower, validation.position());
        activeTowers.add(placedTower);
        occupiedTowerCells.add(validation.cell());
        playerGold -= tower.costGold();
        actionFeedbackMessage = "Built " + tower.id() + " (-" + tower.costGold() + "g)";
    }

    private BuildValidationResult validateBuildPlacement(TowerDefinition tower, Vector3 worldPoint) {
        GridCell cell = toGridCell(worldPoint);
        Vector3 position = toCellCenter(cell);

        if (!gameplayMap.buildableCells().contains(cell) || gameplayMap.blockedCells().contains(cell)) {
            return new BuildValidationResult(false, "Cannot build on that cell", cell, position);
        }
        if (occupiedTowerCells.contains(cell)) {
            return new BuildValidationResult(false, "Cell is already occupied", cell, position);
        }

        double buildDistance = horizontalDistance(builderUnit.position(), position);
        if (buildDistance > builderUnit.definition().buildRangeUnits()) {
            return new BuildValidationResult(false, "Target is out of builder range", cell, position);
        }

        if (playerGold < tower.costGold()) {
            return new BuildValidationResult(false, "Not enough gold", cell, position);
        }

        return new BuildValidationResult(true, "", cell, position);
    }

    private void applySellTowerCommand(SellTowerCommand command) {
        if (command.towerInstanceId() == null || command.towerInstanceId().isBlank()) {
            actionFeedbackMessage = "Select a tower to sell";
            return;
        }

        TowerInstance tower = findTowerByInstanceId(command.towerInstanceId());
        if (tower == null) {
            actionFeedbackMessage = "Tower not found";
            return;
        }

        int refund = (int) Math.round(tower.definition().costGold() * tower.definition().sellRefundRatio());
        activeTowers.remove(tower);
        occupiedTowerCells.remove(toGridCell(tower.position()));
        playerGold += refund;

        if ("tower".equals(selectedEntityType) && selectedEntityId.equals(tower.instanceId())) {
            selectedEntityType = "";
            selectedEntityId = "";
            activeHudActionId = resolveFallbackActionId();
        }
        actionFeedbackMessage = "Sold " + tower.definition().id() + " (+" + refund + "g)";
    }

    private String resolveSellTargetTowerId(Vector3 worldPoint) {
        PickedEntity picked = pickEntityAt(worldPoint);
        if (picked != null && "tower".equals(picked.type())) {
            return picked.id();
        }
        return selectedEntityId;
    }

    private void updateBuildPreview(String towerId, Vector3 worldPoint) {
        TowerDefinition tower = towerCatalog.required(towerId);
        BuildValidationResult validation = validateBuildPlacement(tower, worldPoint);
        buildPreviewState = new BuildPreviewState(tower.id(), validation.position(), validation.valid());
    }

    private boolean isBuildActionActive() {
        UiActionDefinition active = currentActiveAction();
        return active != null && active.mode() == UiActionMode.BUILD;
    }

    private UiActionDefinition currentActiveAction() {
        if (activeHudActionId == null || activeHudActionId.isBlank()) {
            return null;
        }
        try {
            return uiActionCatalog.required(activeHudActionId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isSelectedBuilder() {
        return "builder".equals(selectedEntityType) && builderUnit.instanceId().equals(selectedEntityId);
    }

    private GridCell toGridCell(Vector3 position) {
        int cellX = (int) Math.round(position.x() / grid.cellSize());
        int cellZ = (int) Math.round(position.z() / grid.cellSize());
        return new GridCell(cellX, cellZ);
    }

    private Vector3 toCellCenter(GridCell cell) {
        return new Vector3(cell.x() * grid.cellSize(), 0.0, cell.z() * grid.cellSize());
    }

    private String nextTowerInstanceId() {
        towerSequence++;
        return "tower-" + towerSequence;
    }

    private TowerInstance findTowerByInstanceId(String towerInstanceId) {
        for (TowerInstance tower : activeTowers) {
            if (tower.instanceId().equals(towerInstanceId)) {
                return tower;
            }
        }
        return null;
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
                return new PickedEntity("tower", tower.instanceId());
            }
        }

        return null;
    }

    private double horizontalDistance(Vector3 a, Vector3 b) {
        return Math.sqrt(horizontalDistanceSquared(a, b));
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
                tower.instanceId(),
                tower.definition().id(),
                tower.position(),
                tower.definition().rangeUnits(),
                "tower".equals(selectedEntityType) && tower.instanceId().equals(selectedEntityId)
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

        String selectedType = selectedEntityType.isBlank() ? "none" : selectedEntityType;
        List<HudActionView> hudActionViews = uiActionCatalog.actionsForEntityType(selectedType).stream()
            .map(action -> new HudActionView(
                action.id(),
                labelForAction(action),
                action.hotkey(),
                action.id().equals(activeHudActionId)
            ))
            .toList();

        MapDebugView mapDebugView = new MapDebugView(
            gameplayMap.path(),
            gameplayMap.buildableCells(),
            gameplayMap.blockedCells()
        );

        BuildPreviewView buildPreview = buildPreviewState == null
            ? null
            : new BuildPreviewView(buildPreviewState.towerId(), buildPreviewState.position(), buildPreviewState.valid());

        return new WorldView(
            grid,
            mapDebugView,
            buildPreview,
            towerViews,
            builderViews,
            enemyViews,
            hudActionViews,
            activeHudActionId,
            actionFeedbackMessage,
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

    private String labelForAction(UiActionDefinition action) {
        if (action.mode() != UiActionMode.BUILD) {
            return action.label();
        }
        TowerDefinition tower = towerCatalog.required(action.towerId());
        return action.label() + " (" + tower.costGold() + "g)";
    }

    private record PickedEntity(String type, String id) {
    }

    private record BuildPreviewState(String towerId, Vector3 position, boolean valid) {
    }

    private record BuildValidationResult(boolean valid, String reason, GridCell cell, Vector3 position) {
    }
}
