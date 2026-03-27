package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.gameplay.builder.BuilderCatalog;
import com.swmschmidt.td.core.gameplay.builder.BuilderDefinition;
import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyDefinition;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.map.MapPath;
import com.swmschmidt.td.core.gameplay.map.GridCell;
import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.tower.TowerDefinition;
import com.swmschmidt.td.core.gameplay.uiaction.UiActionCatalog;
import com.swmschmidt.td.core.gameplay.uiaction.UiActionDefinition;
import com.swmschmidt.td.core.gameplay.wave.WaveCatalog;
import com.swmschmidt.td.core.gameplay.wave.WaveDefinition;
import com.swmschmidt.td.core.gameplay.wave.WaveSpawnDefinition;
import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.WorldView;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SandboxSceneTest {

    private static final TowerCatalog TEST_TOWERS = new TowerCatalog(Map.of(
        "arrow",
        new TowerDefinition("arrow", 3.0, 4.0, 2.0, 5, "hitscan")
    ));

    private static final WaveCatalog SINGLE_WAVE = new WaveCatalog(List.of(
        new WaveDefinition("wave_01", List.of(new WaveSpawnDefinition("grunt", 1, 0.5)))
    ));

    private static final BuilderCatalog TEST_BUILDERS = new BuilderCatalog(Map.of(
        "builder",
        new BuilderDefinition("builder", 4.0, 0.4, -1.0, -1.0)
    ));

    private static final UiActionCatalog TEST_UI_ACTIONS = new UiActionCatalog(List.of(
        new UiActionDefinition("move", "Move", "M", Set.of("builder")),
        new UiActionDefinition("build", "Build", "B", Set.of("builder")),
        new UiActionDefinition("cancel", "Cancel", "C", Set.of("none", "builder", "tower"))
    ));

    @Test
    void spawnsEnemyFromWaveAndMovesDeterministicallyAlongPath() {
        SandboxScene scene = new SandboxScene(
            new GridDefinition(8, 1.0),
            new GameplayMap(
                "test-map",
                new MapPath(List.of(new Vector3(0.0, 0.0, 0.0), new Vector3(10.0, 0.0, 0.0))),
                Set.of(new GridCell(0, 1)),
                Set.of()
            ),
            new EnemyCatalog(Map.of("grunt", new EnemyDefinition("grunt", 2.0, 0.3, 10.0, 2))),
            TEST_TOWERS,
            TEST_BUILDERS,
            TEST_UI_ACTIONS,
            SINGLE_WAVE,
            0.0,
            0.0,
            "arrow",
            "builder",
            "move",
            20,
            3,
            () -> false,
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> Optional.empty()
        );

        scene.update(0.4);
        WorldView firstView = scene.captureView();
        assertEquals(0, firstView.enemies().size());

        scene.update(0.2);
        scene.update(0.2);
        scene.update(0.2);
        WorldView secondView = scene.captureView();
        assertEquals(1, secondView.enemies().size());
        assertEquals(0.4, secondView.enemies().getFirst().position().x(), 1e-9);
        assertEquals("IN_WAVE", secondView.matchState());

        scene.update(1.0);
        WorldView thirdView = scene.captureView();
        assertEquals(2.4, thirdView.enemies().getFirst().position().x(), 1e-9);
        assertFalse(thirdView.defeatTriggered());
    }

    @Test
    void triggersDefeatWhenEnemyReachesGoal() {
        SandboxScene scene = new SandboxScene(
            new GridDefinition(8, 1.0),
            new GameplayMap(
                "test-map",
                new MapPath(List.of(new Vector3(0.0, 0.0, 0.0), new Vector3(2.0, 0.0, 0.0))),
                Set.of(new GridCell(1, 1)),
                Set.of()
            ),
            new EnemyCatalog(Map.of("runner", new EnemyDefinition("runner", 4.0, 0.25, 8.0, 3))),
            TEST_TOWERS,
            TEST_BUILDERS,
            TEST_UI_ACTIONS,
            new WaveCatalog(List.of(
                new WaveDefinition("wave_01", List.of(new WaveSpawnDefinition("runner", 1, 0.5)))
            )),
            0.0,
            0.5,
            "arrow",
            "builder",
            "move",
            20,
            1,
            () -> false,
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> Optional.empty()
        );

        scene.update(0.5);
        scene.update(0.5);
        assertTrue(scene.captureView().defeatTriggered());
    }

    @Test
    void placesTowerKillsEnemyAndRewardsGold() {
        AtomicBoolean placeTowerOnce = new AtomicBoolean(true);
        SandboxScene scene = new SandboxScene(
            new GridDefinition(8, 1.0),
            new GameplayMap(
                "test-map",
                new MapPath(List.of(new Vector3(0.0, 0.0, 0.0), new Vector3(8.0, 0.0, 0.0))),
                Set.of(new GridCell(0, 0)),
                Set.of()
            ),
            new EnemyCatalog(Map.of("grunt", new EnemyDefinition("grunt", 0.8, 0.3, 10.0, 7))),
            TEST_TOWERS,
            TEST_BUILDERS,
            TEST_UI_ACTIONS,
            new WaveCatalog(List.of(
                new WaveDefinition("wave_01", List.of(new WaveSpawnDefinition("grunt", 1, 0.1)))
            )),
            0.0,
            0.0,
            "arrow",
            "builder",
            "move",
            10,
            3,
            () -> placeTowerOnce.getAndSet(false),
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> Optional.empty()
        );

        for (int i = 0; i < 20; i++) {
            scene.update(0.25);
        }

        WorldView view = scene.captureView();
        assertEquals(1, view.towers().size());
        assertEquals(0, view.enemies().size());
        assertEquals(12, view.playerGold());
        assertEquals(3, view.playerLives());
        assertFalse(view.defeatTriggered());
        assertTrue(view.victoryTriggered());
    }

    @Test
    void entersVictoryAfterAllWavesAreCleared() {
        SandboxScene scene = new SandboxScene(
            new GridDefinition(8, 1.0),
            new GameplayMap(
                "test-map",
                new MapPath(List.of(new Vector3(0.0, 0.0, 0.0), new Vector3(3.0, 0.0, 0.0))),
                Set.of(new GridCell(1, 1)),
                Set.of()
            ),
            new EnemyCatalog(Map.of("grunt", new EnemyDefinition("grunt", 5.0, 0.3, 1.0, 1))),
            new TowerCatalog(Map.of("arrow", new TowerDefinition("arrow", 4.0, 2.0, 20.0, 1, "hitscan"))),
            TEST_BUILDERS,
            TEST_UI_ACTIONS,
            new WaveCatalog(List.of(
                new WaveDefinition("wave_01", List.of(new WaveSpawnDefinition("grunt", 1, 0.1))),
                new WaveDefinition("wave_02", List.of(new WaveSpawnDefinition("grunt", 1, 0.1)))
            )),
            0.0,
            0.0,
            "arrow",
            "builder",
            "move",
            10,
            3,
            () -> false,
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> Optional.empty(),
            () -> Optional.empty()
        );

        for (int i = 0; i < 8; i++) {
            scene.update(0.2);
        }

        WorldView view = scene.captureView();
        assertTrue(view.victoryTriggered());
        assertEquals("VICTORY", view.matchState());
    }

    @Test
    void selectsBuilderAndMovesItUsingCommandFlow() {
        Queue<Optional<Vector3>> selectEvents = new ArrayDeque<>();
        Queue<Optional<Vector3>> contextEvents = new ArrayDeque<>();
        Supplier<Optional<Vector3>> selectSupplier = () -> Optional.ofNullable(selectEvents.poll()).orElse(Optional.empty());
        Supplier<Optional<Vector3>> contextSupplier = () -> Optional.ofNullable(contextEvents.poll()).orElse(Optional.empty());

        SandboxScene scene = new SandboxScene(
            new GridDefinition(8, 1.0),
            new GameplayMap(
                "test-map",
                new MapPath(List.of(new Vector3(0.0, 0.0, 0.0), new Vector3(10.0, 0.0, 0.0))),
                Set.of(new GridCell(0, 0)),
                Set.of()
            ),
            new EnemyCatalog(Map.of("grunt", new EnemyDefinition("grunt", 2.0, 0.3, 10.0, 2))),
            TEST_TOWERS,
            TEST_BUILDERS,
            TEST_UI_ACTIONS,
            SINGLE_WAVE,
            100.0,
            0.0,
            "arrow",
            "builder",
            "move",
            20,
            3,
            () -> false,
            selectSupplier,
            contextSupplier,
            () -> Optional.empty(),
            () -> Optional.empty()
        );

        selectEvents.add(Optional.of(new Vector3(-1.0, 0.0, -1.0)));
        scene.update(0.1);
        WorldView selectedView = scene.captureView();
        assertEquals("builder", selectedView.selectedEntityType());
        assertEquals("builder-1", selectedView.selectedEntityId());
        assertTrue(selectedView.builders().getFirst().selected());

        contextEvents.add(Optional.of(new Vector3(2.0, 0.0, -1.0)));
        scene.update(0.5);
        scene.update(0.5);

        WorldView movedView = scene.captureView();
        assertEquals(2.0, movedView.builders().getFirst().position().x(), 1e-9);
        assertEquals(-1.0, movedView.builders().getFirst().position().z(), 1e-9);
        assertEquals("builder", movedView.selectedEntityType());
        assertEquals("builder-1", movedView.selectedEntityId());
    }

    @Test
    void switchesHudActionThroughCommandAndHotkeySupplier() {
        Queue<Optional<String>> hudActionEvents = new ArrayDeque<>();
        Queue<Optional<String>> hotkeyActionEvents = new ArrayDeque<>();

        SandboxScene scene = new SandboxScene(
            new GridDefinition(8, 1.0),
            new GameplayMap(
                "test-map",
                new MapPath(List.of(new Vector3(0.0, 0.0, 0.0), new Vector3(10.0, 0.0, 0.0))),
                Set.of(new GridCell(0, 0)),
                Set.of()
            ),
            new EnemyCatalog(Map.of("grunt", new EnemyDefinition("grunt", 2.0, 0.3, 10.0, 2))),
            TEST_TOWERS,
            TEST_BUILDERS,
            TEST_UI_ACTIONS,
            SINGLE_WAVE,
            100.0,
            0.0,
            "arrow",
            "builder",
            "move",
            20,
            3,
            () -> false,
            () -> Optional.of(new Vector3(-1.0, 0.0, -1.0)),
            () -> Optional.empty(),
            () -> Optional.ofNullable(hudActionEvents.poll()).orElse(Optional.empty()),
            () -> Optional.ofNullable(hotkeyActionEvents.poll()).orElse(Optional.empty())
        );

        scene.update(0.1);
        assertEquals("move", scene.captureView().activeHudActionId());

        hudActionEvents.add(Optional.of("build"));
        scene.update(0.1);
        assertEquals("build", scene.captureView().activeHudActionId());

        hotkeyActionEvents.add(Optional.of("move"));
        scene.update(0.1);
        assertEquals("move", scene.captureView().activeHudActionId());
    }
}
