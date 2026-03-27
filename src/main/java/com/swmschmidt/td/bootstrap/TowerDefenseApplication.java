package com.swmschmidt.td.bootstrap;

import com.swmschmidt.td.application.scene.SandboxScene;
import com.swmschmidt.td.core.gameplay.builder.BuilderCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.wave.WaveCatalog;
import com.swmschmidt.td.core.gameloop.GameLoop;
import com.swmschmidt.td.core.input.PointerClick;
import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.SceneManager;
import com.swmschmidt.td.infrastructure.config.AppConfig;
import com.swmschmidt.td.infrastructure.config.AppConfigLoader;
import com.swmschmidt.td.infrastructure.content.BuilderContentLoader;
import com.swmschmidt.td.infrastructure.content.EnemyContentLoader;
import com.swmschmidt.td.infrastructure.content.MapContentLoader;
import com.swmschmidt.td.infrastructure.content.TowerContentLoader;
import com.swmschmidt.td.infrastructure.content.WaveContentLoader;
import com.swmschmidt.td.infrastructure.input.SwingInputService;
import com.swmschmidt.td.infrastructure.rendering.api.FrameRenderer;
import com.swmschmidt.td.infrastructure.rendering.camera.FixedCamera;
import com.swmschmidt.td.infrastructure.rendering.camera.ScreenToWorldRayPicker;
import com.swmschmidt.td.infrastructure.rendering.swing.SoftwareGridRenderer;
import com.swmschmidt.td.infrastructure.rendering.swing.SwingGameWindow;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class TowerDefenseApplication {

    public void start() {
        AppConfig config = new AppConfigLoader().load();
        GridDefinition grid = new GridDefinition(config.gridHalfSize(), config.gridCellSize());

        GameplayMap gameplayMap = new MapContentLoader().load(Path.of(config.mapContentPath()));
        EnemyCatalog enemyCatalog = new EnemyContentLoader().load(Path.of(config.enemyContentPath()));
        TowerCatalog towerCatalog = new TowerContentLoader().load(Path.of(config.towerContentPath()));
        BuilderCatalog builderCatalog = new BuilderContentLoader().load(Path.of(config.builderContentPath()));
        WaveCatalog waveCatalog = new WaveContentLoader().load(Path.of(config.waveContentPath()));

        SwingInputService input = new SwingInputService();

        FixedCamera camera = new FixedCamera(
            config.cameraPosition(),
            config.cameraTarget(),
            config.cameraFovDegrees()
        );
        ScreenToWorldRayPicker worldRayPicker = new ScreenToWorldRayPicker();

        Supplier<Optional<Vector3>> selectWorldPointRequested = () ->
            input.consumeSelectRequested().flatMap(click -> pickWorldPoint(click, worldRayPicker, camera, config));
        Supplier<Optional<Vector3>> contextWorldPointRequested = () ->
            input.consumeContextCommandRequested().flatMap(click -> pickWorldPoint(click, worldRayPicker, camera, config));

        SceneManager sceneManager = new SceneManager(
            new SandboxScene(
                grid,
                gameplayMap,
                enemyCatalog,
                towerCatalog,
                builderCatalog,
                waveCatalog,
                config.preWaveDelaySeconds(),
                config.postWaveDelaySeconds(),
                config.defaultTowerId(),
                config.defaultBuilderId(),
                config.startingGold(),
                config.startingLives(),
                input::consumePlaceTowerRequested,
                selectWorldPointRequested,
                contextWorldPointRequested
            )
        );

        SwingGameWindow window = new SwingGameWindow(
            config.title(),
            config.windowWidth(),
            config.windowHeight()
        );
        window.frame().addKeyListener(input);
        window.renderSurface().addMouseListener(input);
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

    private Optional<Vector3> pickWorldPoint(
        PointerClick click,
        ScreenToWorldRayPicker worldRayPicker,
        FixedCamera camera,
        AppConfig config
    ) {
        return worldRayPicker.pickGround(
            click.x(),
            click.y(),
            config.windowWidth(),
            config.windowHeight(),
            camera
        );
    }
}
