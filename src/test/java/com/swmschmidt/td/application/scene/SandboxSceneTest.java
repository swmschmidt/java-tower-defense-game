package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyDefinition;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.map.MapPath;
import com.swmschmidt.td.core.gameplay.map.GridCell;
import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.tower.TowerDefinition;
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

    @Test
    void spawnsEnemyAndMovesDeterministicallyAlongPath() {
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
            "grunt",
            1.0,
            1,
            "arrow",
            20,
            3,
            () -> false
        );

        scene.update(0.5);
        WorldView firstView = scene.captureView();
        assertEquals(0, firstView.enemies().size());

        scene.update(0.5);
        WorldView secondView = scene.captureView();
        assertEquals(1, secondView.enemies().size());
        assertEquals(1.0, secondView.enemies().getFirst().position().x(), 1e-9);

        scene.update(1.0);
        WorldView thirdView = scene.captureView();
        assertEquals(3.0, thirdView.enemies().getFirst().position().x(), 1e-9);
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
            "runner",
            0.5,
            1,
            "arrow",
            20,
            1,
            () -> false
        );

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
            "grunt",
            0.1,
            1,
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
    }
}
