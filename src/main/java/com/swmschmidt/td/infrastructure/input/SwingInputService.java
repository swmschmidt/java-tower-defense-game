package com.swmschmidt.td.infrastructure.input;

import com.swmschmidt.td.core.input.InputService;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SwingInputService extends KeyAdapter implements InputService {
    private final AtomicBoolean exitRequested = new AtomicBoolean(false);

    @Override
    public void poll() {
        // Swing input events are received asynchronously via keyPressed.
    }

    @Override
    public boolean isExitRequested() {
        return exitRequested.get();
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            exitRequested.set(true);
        }
    }

    public void requestExit() {
        exitRequested.set(true);
    }
}
