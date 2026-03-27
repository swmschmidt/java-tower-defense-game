package com.swmschmidt.td.infrastructure.input;

import com.swmschmidt.td.core.input.InputService;
import com.swmschmidt.td.core.input.PointerClick;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class SwingInputService extends KeyAdapter implements InputService, MouseListener {
    private final AtomicBoolean exitRequested = new AtomicBoolean(false);
    private final AtomicBoolean placeTowerRequested = new AtomicBoolean(false);
    private final AtomicReference<PointerClick> selectRequested = new AtomicReference<>();
    private final AtomicReference<PointerClick> contextCommandRequested = new AtomicReference<>();
    private final AtomicReference<String> hudHotkeyRequested = new AtomicReference<>();

    @Override
    public void poll() {
        // Swing input events are received asynchronously via keyPressed.
    }

    @Override
    public boolean isExitRequested() {
        return exitRequested.get();
    }

    @Override
    public boolean consumePlaceTowerRequested() {
        return placeTowerRequested.getAndSet(false);
    }

    @Override
    public Optional<PointerClick> consumeSelectRequested() {
        return Optional.ofNullable(selectRequested.getAndSet(null));
    }

    @Override
    public Optional<PointerClick> consumeContextCommandRequested() {
        return Optional.ofNullable(contextCommandRequested.getAndSet(null));
    }

    @Override
    public Optional<String> consumeHudHotkeyRequested() {
        return Optional.ofNullable(hudHotkeyRequested.getAndSet(null));
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            exitRequested.set(true);
            return;
        }
        if (event.getKeyCode() == KeyEvent.VK_T) {
            placeTowerRequested.set(true);
            return;
        }

        char keyChar = event.getKeyChar();
        if (!Character.isISOControl(keyChar)) {
            hudHotkeyRequested.set(String.valueOf(Character.toUpperCase(keyChar)));
        }
    }

    public void requestExit() {
        exitRequested.set(true);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
            selectRequested.set(new PointerClick(event.getX(), event.getY()));
            return;
        }
        if (event.getButton() == MouseEvent.BUTTON3) {
            contextCommandRequested.set(new PointerClick(event.getX(), event.getY()));
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }
}
