package com.swmschmidt.td.core.scene;

public record HudActionView(
    String id,
    String label,
    String hotkey,
    boolean selected
) {
}
