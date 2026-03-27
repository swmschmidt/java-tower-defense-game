package com.swmschmidt.td.infrastructure.rendering.swing;

import com.swmschmidt.td.core.scene.HudActionView;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public final class LowerHudLayout {
    private static final int OUTER_MARGIN = 12;
    private static final int SECTION_GAP = 12;
    private static final int HUD_HEIGHT = 170;
    private static final int ACTION_BUTTON_HEIGHT = 32;
    private static final int ACTION_BUTTON_GAP = 8;

    public HudSections sections(int width, int height) {
        int hudY = height - HUD_HEIGHT - OUTER_MARGIN;
        int hudWidth = Math.max(320, width - (OUTER_MARGIN * 2));
        Rectangle outer = new Rectangle(OUTER_MARGIN, hudY, hudWidth, HUD_HEIGHT);

        int leftWidth = (int) (outer.width * 0.24);
        int centerWidth = (int) (outer.width * 0.38);
        int rightWidth = outer.width - leftWidth - centerWidth - (SECTION_GAP * 2);

        Rectangle left = new Rectangle(outer.x + SECTION_GAP, outer.y + SECTION_GAP, leftWidth - SECTION_GAP, outer.height - (SECTION_GAP * 2));
        Rectangle center = new Rectangle(left.x + left.width + SECTION_GAP, left.y, centerWidth, left.height);
        Rectangle right = new Rectangle(center.x + center.width + SECTION_GAP, left.y, rightWidth, left.height);

        return new HudSections(outer, left, center, right);
    }

    public boolean containsHud(int x, int y, int width, int height) {
        return sections(width, height).outer().contains(x, y);
    }

    public String resolveActionIdAt(int x, int y, int width, int height, List<HudActionView> actions) {
        List<ActionButtonLayout> buttons = actionButtons(width, height, actions);
        for (ActionButtonLayout button : buttons) {
            if (button.bounds().contains(x, y)) {
                return button.action().id();
            }
        }
        return null;
    }

    public List<ActionButtonLayout> actionButtons(int width, int height, List<HudActionView> actions) {
        Rectangle right = sections(width, height).right();
        List<ActionButtonLayout> result = new ArrayList<>();

        int y = right.y + 30;
        int buttonWidth = right.width - 24;
        for (HudActionView action : actions) {
            Rectangle bounds = new Rectangle(right.x + 12, y, buttonWidth, ACTION_BUTTON_HEIGHT);
            result.add(new ActionButtonLayout(action, bounds));
            y += ACTION_BUTTON_HEIGHT + ACTION_BUTTON_GAP;
        }
        return result;
    }

    public record HudSections(
        Rectangle outer,
        Rectangle left,
        Rectangle center,
        Rectangle right
    ) {
    }

    public record ActionButtonLayout(HudActionView action, Rectangle bounds) {
    }
}
