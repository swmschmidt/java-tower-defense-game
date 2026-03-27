package com.swmschmidt.td.core.gameloop;

import java.util.function.BooleanSupplier;

public final class GameLoop {
    private final int updatesPerSecond;
    private final int framesPerSecond;
    private final Runnable update;
    private final Runnable render;
    private final BooleanSupplier shouldRun;

    public GameLoop(
        int updatesPerSecond,
        int framesPerSecond,
        Runnable update,
        Runnable render,
        BooleanSupplier shouldRun
    ) {
        this.updatesPerSecond = updatesPerSecond;
        this.framesPerSecond = framesPerSecond;
        this.update = update;
        this.render = render;
        this.shouldRun = shouldRun;
    }

    public void run() {
        long previousNanos = System.nanoTime();
        double updateStep = 1_000_000_000.0 / updatesPerSecond;
        double frameStep = 1_000_000_000.0 / framesPerSecond;
        double updateAccumulator = 0.0;
        double frameAccumulator = 0.0;

        while (shouldRun.getAsBoolean()) {
            long now = System.nanoTime();
            long elapsed = now - previousNanos;
            previousNanos = now;

            updateAccumulator += elapsed;
            frameAccumulator += elapsed;

            while (updateAccumulator >= updateStep) {
                update.run();
                updateAccumulator -= updateStep;
            }

            if (frameAccumulator >= frameStep) {
                render.run();
                frameAccumulator = 0.0;
            }

            Thread.onSpinWait();
        }
    }
}
