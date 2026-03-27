package com.swmschmidt.td.bootstrap;

import com.swmschmidt.td.application.scene.SandboxScene;
import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameloop.GameLoop;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.SceneManager;
import com.swmschmidt.td.infrastructure.config.AppConfig;
import com.swmschmidt.td.infrastructure.config.AppConfigLoader;
import com.swmschmidt.td.infrastructure.content.EnemyContentLoader;
import com.swmschmidt.td.infrastructure.content.MapContentLoader;
import com.swmschmidt.td.infrastructure.input.SwingInputService;
import com.swmschmidt.td.infrastructure.rendering.api.FrameRenderer;
import com.swmschmidt.td.infrastructure.rendering.camera.FixedCamera;
import com.swmschmidt.td.infrastructure.rendering.swing.SoftwareGridRenderer;
import com.swmschmidt.td.infrastructure.rendering.swing.SwingGameWindow;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TowerDefenseApplication {

    public void start() {
        AppConfig config = new AppConfigLoader().load();
        GridDefinition grid = new GridDefinition(config.gridHalfSize(), config.gridCellSize());

        GameplayMap gameplayMap = new MapContentLoader().load(Path.of(config.mapContentPath()));
        EnemyCatalog enemyCatalog = new EnemyContentLoader().load(Path.of(config.enemyContentPath()));

        SceneManager sceneManager = new SceneManager(
            new SandboxScene(
                grid,
                gameplayMap,
                enemyCatalog,
                config.spawnEnemyId(),
                config.spawnIntervalSeconds(),
                config.spawnMaxCount()
            )
        );

        FixedCamera camera = new FixedCamera(
            config.cameraPosition(),
            config.cameraTarget(),
            config.cameraFovDegrees()
        );

        SwingGameWindow window = new SwingGameWindow(
            config.title(),
            config.windowWidth(),
            config.windowHeight()
        );
        SwingInputService input = new SwingInputService();
        window.frame().addKeyListener(input);
        window.showWindow();

        FrameRenderer renderer = new SoftwareGridRenderer();
        AtomicBoolean running = new AtomicBoolean(true);
        double fixedDeltaSeconds = 1.0 / config.updatesPerSecond();

        GameLoop loop = new GameLoop(
            config.updatesPerSecond(),
            config.framesPerSecond(),
            () -> {
                input.poll();
                sceneManager.update(fixedDeltaSeconds);
                if (input.isExitRequested() || !window.isOpen()) {
                    running.set(false);
                }
            },
            () -> {
                BufferedImage frame = renderer.render(
                    sceneManager.captureView(),
                    camera,
                    config.windowWidth(),
                    config.windowHeight()
                );
                window.present(frame);
            },
            () -> running.get() && window.isOpen()
        );

        try {
            loop.run();
        } finally {
            window.close();
        }
    }
}
