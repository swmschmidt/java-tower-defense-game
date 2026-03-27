package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyDefinition;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.map.MapPath;
import com.swmschmidt.td.core.gameplay.map.GridCell;
import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.tower.TowerDefinition;
import com.swmschmidt.td.core.gameplay.wave.WaveCatalog;
import com.swmschmidt.td.core.gameplay.wave.WaveDefinition;
import com.swmschmidt.td.core.gameplay.wave.WaveSpawnDefinition;
import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.WorldView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
            SINGLE_WAVE,
            0.0,
            0.0,
            "arrow",
            20,
            3,
            () -> false
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
            new WaveCatalog(List.of(
                new WaveDefinition("wave_01", List.of(new WaveSpawnDefinition("runner", 1, 0.5)))
            )),
            0.0,
            0.5,
            "arrow",
            20,
            1,
            () -> false
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
            new WaveCatalog(List.of(
                new WaveDefinition("wave_01", List.of(new WaveSpawnDefinition("grunt", 1, 0.1)))
            )),
            0.0,
            0.0,
            "arrow",
            10,
            3,
            () -> placeTowerOnce.getAndSet(false)
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
            new WaveCatalog(List.of(
                new WaveDefinition("wave_01", List.of(new WaveSpawnDefinition("grunt", 1, 0.1))),
                new WaveDefinition("wave_02", List.of(new WaveSpawnDefinition("grunt", 1, 0.1)))
            )),
            0.0,
            0.0,
            "arrow",
            10,
            3,
            () -> false
        );

        for (int i = 0; i < 8; i++) {
            scene.update(0.2);
        }

        WorldView view = scene.captureView();
        assertTrue(view.victoryTriggered());
        assertEquals("VICTORY", view.matchState());
    }
}
