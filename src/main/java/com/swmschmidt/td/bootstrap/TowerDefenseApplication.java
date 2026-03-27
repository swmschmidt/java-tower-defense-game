package com.swmschmidt.td.bootstrap;

import com.swmschmidt.td.application.scene.SandboxScene;
import com.swmschmidt.td.core.gameplay.builder.BuilderCatalog;
import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import com.swmschmidt.td.core.gameplay.uiaction.UiActionCatalog;
import com.swmschmidt.td.core.gameplay.wave.WaveCatalog;
import com.swmschmidt.td.core.gameloop.GameLoop;
import com.swmschmidt.td.core.input.PointerClick;
import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.SceneManager;
import com.swmschmidt.td.core.scene.WorldView;
import com.swmschmidt.td.infrastructure.config.AppConfig;
import com.swmschmidt.td.infrastructure.config.AppConfigLoader;
import com.swmschmidt.td.infrastructure.content.BuilderContentLoader;
import com.swmschmidt.td.infrastructure.content.EnemyContentLoader;
import com.swmschmidt.td.infrastructure.content.MapContentLoader;
import com.swmschmidt.td.infrastructure.content.TowerContentLoader;
import com.swmschmidt.td.infrastructure.content.UiActionContentLoader;
import com.swmschmidt.td.infrastructure.content.WaveContentLoader;
import com.swmschmidt.td.infrastructure.input.SwingInputService;
import com.swmschmidt.td.infrastructure.rendering.api.FrameRenderer;
import com.swmschmidt.td.infrastructure.rendering.camera.FixedCamera;
import com.swmschmidt.td.infrastructure.rendering.camera.ScreenToWorldRayPicker;
import com.swmschmidt.td.infrastructure.rendering.swing.LowerHudLayout;
import com.swmschmidt.td.infrastructure.rendering.swing.SoftwareGridRenderer;
import com.swmschmidt.td.infrastructure.rendering.swing.SwingGameWindow;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

public final class TowerDefenseApplication {

    public void start() {
        AppConfig config = new AppConfigLoader().load();
        GridDefinition grid = new GridDefinition(config.gridHalfSize(), config.gridCellSize());

        GameplayMap gameplayMap = new MapContentLoader().load(Path.of(config.mapContentPath()));
        EnemyCatalog enemyCatalog = new EnemyContentLoader().load(Path.of(config.enemyContentPath()));
        TowerCatalog towerCatalog = new TowerContentLoader().load(Path.of(config.towerContentPath()));
        BuilderCatalog builderCatalog = new BuilderContentLoader().load(Path.of(config.builderContentPath()));
        UiActionCatalog uiActionCatalog = new UiActionContentLoader().load(Path.of(config.uiActionContentPath()));
        WaveCatalog waveCatalog = new WaveContentLoader().load(Path.of(config.waveContentPath()));

        SwingInputService input = new SwingInputService();

        FixedCamera camera = new FixedCamera(
            config.cameraPosition(),
            config.cameraTarget(),
            config.cameraFovDegrees()
        );
        ScreenToWorldRayPicker worldRayPicker = new ScreenToWorldRayPicker();
        LowerHudLayout hudLayout = new LowerHudLayout();

        Queue<Optional<Vector3>> selectWorldPointQueue = new ArrayDeque<>();
        Queue<Optional<Vector3>> contextWorldPointQueue = new ArrayDeque<>();
        Queue<Optional<String>> hudActionQueue = new ArrayDeque<>();
        Queue<Optional<String>> hudHotkeyActionQueue = new ArrayDeque<>();

        Supplier<Optional<Vector3>> selectWorldPointRequested = () ->
            Optional.ofNullable(selectWorldPointQueue.poll()).orElse(Optional.empty());
        Supplier<Optional<Vector3>> contextWorldPointRequested = () ->
            Optional.ofNullable(contextWorldPointQueue.poll()).orElse(Optional.empty());
        Supplier<Optional<String>> hudActionRequested = () ->
            Optional.ofNullable(hudActionQueue.poll()).orElse(Optional.empty());
        Supplier<Optional<String>> hudHotkeyActionRequested = () ->
            Optional.ofNullable(hudHotkeyActionQueue.poll()).orElse(Optional.empty());

        SceneManager sceneManager = new SceneManager(
            new SandboxScene(
                grid,
                gameplayMap,
                enemyCatalog,
                towerCatalog,
                builderCatalog,
                uiActionCatalog,
                waveCatalog,
                config.preWaveDelaySeconds(),
                config.postWaveDelaySeconds(),
                config.defaultTowerId(),
                config.defaultBuilderId(),
                config.defaultHudActionId(),
                config.startingGold(),
                config.startingLives(),
                input::consumePlaceTowerRequested,
                selectWorldPointRequested,
                contextWorldPointRequested,
                hudActionRequested,
                hudHotkeyActionRequested
            )
        );

        SwingGameWindow window = new SwingGameWindow(
            config.title(),
            config.windowWidth(),
            config.windowHeight()
        );
        window.renderSurface().addKeyListener(input);
        window.renderSurface().addMouseListener(input);
        window.showWindow();
        SwingUtilities.invokeLater(() -> window.renderSurface().requestFocusInWindow());

        FrameRenderer renderer = new SoftwareGridRenderer();
        AtomicBoolean running = new AtomicBoolean(true);
        double fixedDeltaSeconds = 1.0 / config.updatesPerSecond();

        GameLoop loop = new GameLoop(
            config.updatesPerSecond(),
            config.framesPerSecond(),
            () -> {
                input.poll();
                int viewportWidth = resolveViewportDimension(window.renderSurface().getWidth(), config.windowWidth());
                int viewportHeight = resolveViewportDimension(window.renderSurface().getHeight(), config.windowHeight());
                routeHudAndWorldInput(
                    input,
                    sceneManager.captureView(),
                    viewportWidth,
                    viewportHeight,
                    worldRayPicker,
                    camera,
                    hudLayout,
                    uiActionCatalog,
                    selectWorldPointQueue,
                    contextWorldPointQueue,
                    hudActionQueue,
                    hudHotkeyActionQueue
                );
                sceneManager.update(fixedDeltaSeconds);
                if (input.isExitRequested() || !window.isOpen()) {
                    running.set(false);
                }
            },
            () -> {
                int viewportWidth = resolveViewportDimension(window.renderSurface().getWidth(), config.windowWidth());
                int viewportHeight = resolveViewportDimension(window.renderSurface().getHeight(), config.windowHeight());
                BufferedImage frame = renderer.render(
                    sceneManager.captureView(),
                    camera,
                    viewportWidth,
                    viewportHeight
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

    private void routeHudAndWorldInput(
        SwingInputService input,
        WorldView currentView,
        int viewportWidth,
        int viewportHeight,
        ScreenToWorldRayPicker worldRayPicker,
        FixedCamera camera,
        LowerHudLayout hudLayout,
        UiActionCatalog uiActionCatalog,
        Queue<Optional<Vector3>> selectWorldPointQueue,
        Queue<Optional<Vector3>> contextWorldPointQueue,
        Queue<Optional<String>> hudActionQueue,
        Queue<Optional<String>> hudHotkeyActionQueue
    ) {
        input.consumeSelectRequested().ifPresent(click -> {
            String actionId = hudLayout.resolveActionIdAt(
                click.x(),
                click.y(),
                viewportWidth,
                viewportHeight,
                currentView.hudActions()
            );
            if (actionId != null) {
                hudActionQueue.add(Optional.of(actionId));
            } else if (!hudLayout.containsHud(click.x(), click.y(), viewportWidth, viewportHeight)) {
                selectWorldPointQueue.add(pickWorldPoint(click, worldRayPicker, camera, viewportWidth, viewportHeight));
            }
        });

        input.consumeContextCommandRequested().ifPresent(click -> {
            if (!hudLayout.containsHud(click.x(), click.y(), viewportWidth, viewportHeight)) {
                contextWorldPointQueue.add(pickWorldPoint(click, worldRayPicker, camera, viewportWidth, viewportHeight));
            }
        });

        input.consumeHudHotkeyRequested().ifPresent(hotkey -> {
            String actionId = uiActionCatalog.resolveActionIdForHotkey(currentView.selectedEntityType(), hotkey);
            if (actionId != null) {
                hudHotkeyActionQueue.add(Optional.of(actionId));
            }
        });
    }

    private Optional<Vector3> pickWorldPoint(
        PointerClick click,
        ScreenToWorldRayPicker worldRayPicker,
        FixedCamera camera,
        int viewportWidth,
        int viewportHeight
    ) {
        return worldRayPicker.pickGround(
            click.x(),
            click.y(),
            viewportWidth,
            viewportHeight,
            camera
        );
    }

    private int resolveViewportDimension(int measuredValue, int fallbackValue) {
        return measuredValue > 0 ? measuredValue : fallbackValue;
    }
}
