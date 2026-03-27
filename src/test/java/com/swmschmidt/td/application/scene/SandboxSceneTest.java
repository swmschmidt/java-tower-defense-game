package com.swmschmidt.td.application.scene;

import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyDefinition;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.map.MapPath;
import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.WorldView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SandboxSceneTest {

    @Test
    void spawnsEnemyAndMovesDeterministicallyAlongPath() {
        SandboxScene scene = new SandboxScene(
            new GridDefinition(8, 1.0),
            new GameplayMap(
                "test-map",
                new MapPath(List.of(new Vector3(0.0, 0.0, 0.0), new Vector3(10.0, 0.0, 0.0))),
                Set.of(),
                Set.of()
            ),
            new EnemyCatalog(Map.of("grunt", new EnemyDefinition("grunt", 2.0, 0.3))),
            "grunt",
            1.0,
            1
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
                Set.of(),
                Set.of()
            ),
            new EnemyCatalog(Map.of("runner", new EnemyDefinition("runner", 4.0, 0.25))),
            "runner",
            0.5,
            1
        );

        scene.update(0.5);
        assertTrue(scene.captureView().defeatTriggered());
    }
}
