package com.swmschmidt.td.infrastructure.input;

import com.swmschmidt.td.core.input.PointerClick;
import org.junit.jupiter.api.Test;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwingInputServiceTest {

    @Test
    void mapsHotkeysToUppercaseAndConsumesOnce() {
        SwingInputService input = new SwingInputService();
        Canvas source = new Canvas();

        input.keyPressed(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_B, 'b'));

        assertEquals("B", input.consumeHudHotkeyRequested().orElseThrow());
        assertTrue(input.consumeHudHotkeyRequested().isEmpty());
    }

    @Test
    void ignoresControlCharacterHotkeys() {
        SwingInputService input = new SwingInputService();
        Canvas source = new Canvas();

        input.keyPressed(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n'));

        assertTrue(input.consumeHudHotkeyRequested().isEmpty());
    }

    @Test
    void capturesPrimaryAndSecondaryMouseClicksSeparately() {
        SwingInputService input = new SwingInputService();
        Canvas source = new Canvas();

        input.mouseClicked(new MouseEvent(
            source,
            MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(),
            0,
            120,
            240,
            1,
            false,
            MouseEvent.BUTTON1
        ));
        input.mouseClicked(new MouseEvent(
            source,
            MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(),
            0,
            512,
            320,
            1,
            false,
            MouseEvent.BUTTON3
        ));

        PointerClick selectClick = input.consumeSelectRequested().orElseThrow();
        PointerClick contextClick = input.consumeContextCommandRequested().orElseThrow();

        assertEquals(120, selectClick.x());
        assertEquals(240, selectClick.y());
        assertEquals(512, contextClick.x());
        assertEquals(320, contextClick.y());
        assertTrue(input.consumeSelectRequested().isEmpty());
        assertTrue(input.consumeContextCommandRequested().isEmpty());
    }

    @Test
    void tracksLegacyPlaceTowerShortcutAndEscape() {
        SwingInputService input = new SwingInputService();
        Canvas source = new Canvas();

        input.keyPressed(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_T, 't'));
        input.keyPressed(new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));

        assertTrue(input.consumePlaceTowerRequested());
        assertFalse(input.consumePlaceTowerRequested());
        assertTrue(input.isExitRequested());
    }
}
