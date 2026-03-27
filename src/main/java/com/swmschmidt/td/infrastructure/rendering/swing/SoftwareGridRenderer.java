package com.swmschmidt.td.infrastructure.rendering.swing;

import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.BuilderView;
import com.swmschmidt.td.core.scene.EnemyView;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.HudActionView;
import com.swmschmidt.td.core.scene.MapDebugView;
import com.swmschmidt.td.core.scene.TowerView;
import com.swmschmidt.td.core.scene.WorldView;
import com.swmschmidt.td.infrastructure.rendering.api.FrameRenderer;
import com.swmschmidt.td.infrastructure.rendering.camera.FixedCamera;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

public final class SoftwareGridRenderer implements FrameRenderer {
    private static final double NEAR_PLANE = 0.1;
    private final LowerHudLayout lowerHudLayout;

    public SoftwareGridRenderer() {
        this.lowerHudLayout = new LowerHudLayout();
    }

    @Override
    public BufferedImage render(WorldView worldView, FixedCamera camera, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(graphics, width, height);
        drawGroundGrid(graphics, worldView.grid(), camera, width, height);

        if (worldView.mapDebugView() != null) {
            drawMapDebug(graphics, worldView.mapDebugView(), worldView.grid(), camera, width, height);
        }
        drawTowers(graphics, worldView.towers(), camera, width, height);
        drawBuilders(graphics, worldView.builders(), camera, width, height);
        drawEnemies(graphics, worldView.enemies(), camera, width, height);
        drawTopStatus(
            graphics,
            worldView.currentWave(),
            worldView.totalWaves(),
            worldView.matchState(),
            worldView.playerGold(),
            worldView.playerLives()
        );
        drawLowerHud(
            graphics,
            worldView,
            width,
            height
        );
        if (worldView.defeatTriggered()) {
            drawDefeatLabel(graphics, width);
        }
        if (worldView.victoryTriggered()) {
            drawVictoryLabel(graphics, width);
        }

        graphics.dispose();
        return image;
    }

    private void drawDefeatLabel(Graphics2D graphics, int width) {
        graphics.setColor(new Color(204, 64, 64));
        graphics.setFont(graphics.getFont().deriveFont(28f));
        graphics.drawString("Defeat: an enemy reached the goal", Math.max(16, width / 2 - 220), 36);
    }

    private void drawVictoryLabel(Graphics2D graphics, int width) {
        graphics.setColor(new Color(88, 186, 102));
        graphics.setFont(graphics.getFont().deriveFont(28f));
        graphics.drawString("Victory: all waves cleared", Math.max(16, width / 2 - 180), 36);
    }

    private void drawBackground(Graphics2D graphics, int width, int height) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(35, 47, 74), 0, height, new Color(17, 22, 33));
        graphics.setPaint(sky);
        graphics.fillRect(0, 0, width, height);
    }

    private void drawTopStatus(
        Graphics2D graphics,
        int currentWave,
        int totalWaves,
        String matchState,
        int gold,
        int lives
    ) {
        graphics.setColor(new Color(16, 20, 30, 170));
        graphics.fillRoundRect(14, 14, 290, 92, 12, 12);
        graphics.setColor(new Color(224, 228, 235));
        graphics.setFont(graphics.getFont().deriveFont(16f));
        graphics.drawString("Wave: " + currentWave + " / " + totalWaves, 24, 38);
        graphics.drawString("State: " + matchState, 24, 58);
        graphics.drawString("Gold: " + gold, 24, 78);
        graphics.drawString("Lives: " + lives, 160, 78);
    }

    private void drawLowerHud(Graphics2D graphics, WorldView worldView, int width, int height) {
        LowerHudLayout.HudSections sections = lowerHudLayout.sections(width, height);
        Rectangle outer = sections.outer();
        Rectangle left = sections.left();
        Rectangle center = sections.center();
        Rectangle right = sections.right();

        graphics.setColor(new Color(15, 17, 24, 220));
        graphics.fillRoundRect(outer.x, outer.y, outer.width, outer.height, 14, 14);
        graphics.setColor(new Color(68, 74, 90));
        graphics.drawRoundRect(outer.x, outer.y, outer.width, outer.height, 14, 14);

        drawMinimapPlaceholder(graphics, left, worldView);
        drawSelectedEntityPanel(graphics, center, worldView);
        drawActionPanel(graphics, right, worldView.hudActions(), width, height);
    }

    private void drawMinimapPlaceholder(Graphics2D graphics, Rectangle area, WorldView worldView) {
        graphics.setColor(new Color(26, 32, 42, 220));
        graphics.fillRoundRect(area.x, area.y, area.width, area.height, 10, 10);
        graphics.setColor(new Color(84, 93, 110));
        graphics.drawRoundRect(area.x, area.y, area.width, area.height, 10, 10);

        int mapPadding = 12;
        Rectangle mapRect = new Rectangle(area.x + mapPadding, area.y + 26, area.width - (mapPadding * 2), area.height - 38);
        graphics.setColor(new Color(34, 45, 58));
        graphics.fillRect(mapRect.x, mapRect.y, mapRect.width, mapRect.height);
        graphics.setColor(new Color(113, 129, 152));
        graphics.drawRect(mapRect.x, mapRect.y, mapRect.width, mapRect.height);

        graphics.setColor(new Color(224, 228, 235));
        graphics.setFont(graphics.getFont().deriveFont(13f));
        graphics.drawString("Minimap", area.x + 10, area.y + 18);
        graphics.setFont(graphics.getFont().deriveFont(11f));
        graphics.drawString("placeholder", area.x + 10, area.y + area.height - 8);

        if (!worldView.builders().isEmpty()) {
            graphics.setColor(new Color(112, 225, 181));
            int px = mapRect.x + mapRect.width / 2;
            int py = mapRect.y + mapRect.height / 2;
            graphics.fillOval(px - 3, py - 3, 6, 6);
        }
    }

    private void drawSelectedEntityPanel(Graphics2D graphics, Rectangle area, WorldView worldView) {
        graphics.setColor(new Color(26, 32, 42, 220));
        graphics.fillRoundRect(area.x, area.y, area.width, area.height, 10, 10);
        graphics.setColor(new Color(84, 93, 110));
        graphics.drawRoundRect(area.x, area.y, area.width, area.height, 10, 10);

        String selectedTitle = worldView.selectedEntityType().isBlank()
            ? "No Selection"
            : worldView.selectedEntityType() + " / " + worldView.selectedEntityId();

        int portraitSize = Math.min(86, area.height - 40);
        int portraitX = area.x + 12;
        int portraitY = area.y + 28;
        graphics.setColor(new Color(48, 56, 72));
        graphics.fillRect(portraitX, portraitY, portraitSize, portraitSize);
        graphics.setColor(new Color(120, 132, 154));
        graphics.drawRect(portraitX, portraitY, portraitSize, portraitSize);
        graphics.setColor(new Color(222, 228, 237));
        graphics.setFont(graphics.getFont().deriveFont(10f));
        graphics.drawString("image", portraitX + 24, portraitY + portraitSize / 2);

        int textX = portraitX + portraitSize + 12;
        graphics.setColor(new Color(224, 228, 235));
        graphics.setFont(graphics.getFont().deriveFont(13f));
        graphics.drawString("Selected Unit", area.x + 12, area.y + 18);
        graphics.setFont(graphics.getFont().deriveFont(12f));
        graphics.drawString(selectedTitle, textX, portraitY + 16);
        graphics.drawString("HP: placeholder", textX, portraitY + 36);
        graphics.drawString("Action: " + worldView.activeHudActionId(), textX, portraitY + 56);
        graphics.drawString("Left-click to select", textX, portraitY + 76);
    }

    private void drawActionPanel(
        Graphics2D graphics,
        Rectangle area,
        List<HudActionView> actions,
        int width,
        int height
    ) {
        graphics.setColor(new Color(26, 32, 42, 220));
        graphics.fillRoundRect(area.x, area.y, area.width, area.height, 10, 10);
        graphics.setColor(new Color(84, 93, 110));
        graphics.drawRoundRect(area.x, area.y, area.width, area.height, 10, 10);
        graphics.setColor(new Color(224, 228, 235));
        graphics.setFont(graphics.getFont().deriveFont(13f));
        graphics.drawString("Actions", area.x + 10, area.y + 18);

        for (LowerHudLayout.ActionButtonLayout buttonLayout : lowerHudLayout.actionButtons(width, height, actions)) {
            HudActionView action = buttonLayout.action();
            Rectangle bounds = buttonLayout.bounds();

            graphics.setColor(action.selected() ? new Color(84, 122, 184) : new Color(52, 64, 84));
            graphics.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            graphics.setColor(new Color(140, 156, 184));
            graphics.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            graphics.setColor(new Color(236, 239, 245));
            graphics.setFont(graphics.getFont().deriveFont(12f));
            graphics.drawString(action.label(), bounds.x + 10, bounds.y + 20);
            graphics.setFont(graphics.getFont().deriveFont(11f));
            graphics.drawString("[" + action.hotkey() + "]", bounds.x + bounds.width - 34, bounds.y + 20);
        }

        graphics.setColor(new Color(170, 181, 198));
        graphics.setFont(graphics.getFont().deriveFont(11f));
        graphics.drawString("Right-click world to execute Move", area.x + 10, area.y + area.height - 10);
    }

    private void drawGroundGrid(Graphics2D graphics, GridDefinition grid, FixedCamera camera, int width, int height) {
        graphics.setStroke(new BasicStroke(1f));
        graphics.setColor(new Color(79, 112, 98));

        int half = grid.halfSize();
        double cell = grid.cellSize();

        for (int i = -half; i <= half; i++) {
            drawLine(
                graphics,
                new Vector3(i * cell, 0.0, -half * cell),
                new Vector3(i * cell, 0.0, half * cell),
                camera,
                width,
                height
            );
            drawLine(
                graphics,
                new Vector3(-half * cell, 0.0, i * cell),
                new Vector3(half * cell, 0.0, i * cell),
                camera,
                width,
                height
            );
        }

        graphics.setStroke(new BasicStroke(2f));
        graphics.setColor(new Color(180, 219, 201));
        drawLine(
            graphics,
            new Vector3(-half * cell, 0.0, -half * cell),
            new Vector3(half * cell, 0.0, -half * cell),
            camera,
            width,
            height
        );
        drawLine(
            graphics,
            new Vector3(half * cell, 0.0, -half * cell),
            new Vector3(half * cell, 0.0, half * cell),
            camera,
            width,
            height
        );
        drawLine(
            graphics,
            new Vector3(half * cell, 0.0, half * cell),
            new Vector3(-half * cell, 0.0, half * cell),
            camera,
            width,
            height
        );
        drawLine(
            graphics,
            new Vector3(-half * cell, 0.0, half * cell),
            new Vector3(-half * cell, 0.0, -half * cell),
            camera,
            width,
            height
        );
    }

    private void drawMapDebug(
        Graphics2D graphics,
        MapDebugView mapDebugView,
        GridDefinition grid,
        FixedCamera camera,
        int width,
        int height
    ) {
        mapDebugView.buildableCells().forEach(cell ->
            drawGridCellOverlay(graphics, cell.x(), cell.z(), grid.cellSize(), new Color(68, 134, 88, 64), camera, width, height)
        );
        mapDebugView.blockedCells().forEach(cell ->
            drawGridCellOverlay(graphics, cell.x(), cell.z(), grid.cellSize(), new Color(172, 66, 66, 96), camera, width, height)
        );

        graphics.setStroke(new BasicStroke(3f));
        graphics.setColor(new Color(240, 210, 104));
        List<Vector3> waypoints = mapDebugView.path().waypoints();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            drawLine(graphics, lift(waypoints.get(i), 0.05), lift(waypoints.get(i + 1), 0.05), camera, width, height);
        }
    }

    private void drawGridCellOverlay(
        Graphics2D graphics,
        int cellX,
        int cellZ,
        double cellSize,
        Color color,
        FixedCamera camera,
        int width,
        int height
    ) {
        double centerX = cellX * cellSize;
        double centerZ = cellZ * cellSize;
        double half = cellSize * 0.5;

        ProjectedPoint p1 = project(new Vector3(centerX - half, 0.02, centerZ - half), camera, width, height);
        ProjectedPoint p2 = project(new Vector3(centerX + half, 0.02, centerZ - half), camera, width, height);
        ProjectedPoint p3 = project(new Vector3(centerX + half, 0.02, centerZ + half), camera, width, height);
        ProjectedPoint p4 = project(new Vector3(centerX - half, 0.02, centerZ + half), camera, width, height);

        if (!p1.visible || !p2.visible || !p3.visible || !p4.visible) {
            return;
        }

        int[] xs = new int[] { p1.x, p2.x, p3.x, p4.x };
        int[] ys = new int[] { p1.y, p2.y, p3.y, p4.y };
        graphics.setColor(color);
        graphics.fillPolygon(xs, ys, 4);
        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        graphics.drawPolygon(xs, ys, 4);
    }

    private void drawEnemies(
        Graphics2D graphics,
        List<EnemyView> enemies,
        FixedCamera camera,
        int width,
        int height
    ) {
        graphics.setColor(new Color(206, 94, 70));
        for (EnemyView enemy : enemies) {
            drawEnemy(graphics, enemy, camera, width, height);
        }
    }

    private void drawTowers(
        Graphics2D graphics,
        List<TowerView> towers,
        FixedCamera camera,
        int width,
        int height
    ) {
        graphics.setColor(new Color(93, 132, 214));
        for (TowerView tower : towers) {
            Vector3 position = lift(tower.position(), 0.22);
            ProjectedPoint center = project(position, camera, width, height);
            if (!center.visible) {
                continue;
            }

            ProjectedPoint radiusPoint = project(
                lift(tower.position().add(new Vector3(0.35, 0.0, 0.0)), 0.22),
                camera,
                width,
                height
            );
            if (!radiusPoint.visible) {
                continue;
            }

            int radiusPixels = Math.max(4, Math.abs(radiusPoint.x - center.x));
            int diameter = radiusPixels * 2;
            graphics.fillOval(center.x - radiusPixels, center.y - radiusPixels, diameter, diameter);
            graphics.setColor(new Color(195, 217, 255));
            graphics.drawOval(center.x - radiusPixels, center.y - radiusPixels, diameter, diameter);
            graphics.setColor(new Color(93, 132, 214));
        }
    }

    private void drawBuilders(
        Graphics2D graphics,
        List<BuilderView> builders,
        FixedCamera camera,
        int width,
        int height
    ) {
        for (BuilderView builder : builders) {
            Vector3 position = lift(builder.position(), 0.2);
            ProjectedPoint center = project(position, camera, width, height);
            if (!center.visible) {
                continue;
            }

            ProjectedPoint radiusPoint = project(
                lift(builder.position().add(new Vector3(builder.selectionRadius(), 0.0, 0.0)), 0.2),
                camera,
                width,
                height
            );
            if (!radiusPoint.visible) {
                continue;
            }

            int radiusPixels = Math.max(4, Math.abs(radiusPoint.x - center.x));
            int diameter = radiusPixels * 2;

            graphics.setColor(new Color(96, 208, 164));
            graphics.fillOval(center.x - radiusPixels, center.y - radiusPixels, diameter, diameter);
            graphics.setColor(new Color(210, 255, 238));
            graphics.drawOval(center.x - radiusPixels, center.y - radiusPixels, diameter, diameter);

            if (builder.selected()) {
                graphics.setColor(new Color(255, 242, 122));
                int ringRadius = radiusPixels + 6;
                int ringDiameter = ringRadius * 2;
                graphics.drawOval(center.x - ringRadius, center.y - ringRadius, ringDiameter, ringDiameter);
            }
        }
    }

    private void drawEnemy(
        Graphics2D graphics,
        EnemyView enemy,
        FixedCamera camera,
        int width,
        int height
    ) {
        Vector3 center3d = lift(enemy.position(), 0.2);
        ProjectedPoint center = project(center3d, camera, width, height);
        if (!center.visible) {
            return;
        }

        ProjectedPoint radiusPoint = project(
            lift(enemy.position().add(new Vector3(enemy.radius(), 0.0, 0.0)), 0.2),
            camera,
            width,
            height
        );
        if (!radiusPoint.visible) {
            return;
        }

        int radiusPixels = Math.max(3, Math.abs(radiusPoint.x - center.x));
        int diameter = radiusPixels * 2;

        graphics.fillOval(center.x - radiusPixels, center.y - radiusPixels, diameter, diameter);
        graphics.setColor(new Color(245, 214, 178));
        graphics.drawOval(center.x - radiusPixels, center.y - radiusPixels, diameter, diameter);
        graphics.setColor(new Color(206, 94, 70));
    }

    private Vector3 lift(Vector3 point, double yOffset) {
        return new Vector3(point.x(), point.y() + yOffset, point.z());
    }

    private void drawLine(
        Graphics2D graphics,
        Vector3 worldStart,
        Vector3 worldEnd,
        FixedCamera camera,
        int width,
        int height
    ) {
        ProjectedPoint start = project(worldStart, camera, width, height);
        ProjectedPoint end = project(worldEnd, camera, width, height);
        if (!start.visible || !end.visible) {
            return;
        }
        graphics.drawLine(start.x, start.y, end.x, end.y);
    }

    private ProjectedPoint project(Vector3 worldPoint, FixedCamera camera, int width, int height) {
        Vector3 forward = camera.target().subtract(camera.position()).normalize();
        Vector3 right = forward.cross(Vector3.UP).normalize();
        Vector3 up = right.cross(forward).normalize();

        Vector3 relative = worldPoint.subtract(camera.position());
        double cameraX = relative.dot(right);
        double cameraY = relative.dot(up);
        double cameraZ = relative.dot(forward);

        if (cameraZ <= NEAR_PLANE) {
            return ProjectedPoint.hidden();
        }

        double fovRadians = Math.toRadians(camera.fovDegrees());
        double focal = 1.0 / Math.tan(fovRadians * 0.5);
        double normalizedX = (cameraX * focal) / cameraZ;
        double normalizedY = (cameraY * focal) / cameraZ;

        int screenX = (int) ((normalizedX * height * 0.5) + (width * 0.5));
        int screenY = (int) ((-normalizedY * height * 0.5) + (height * 0.5));

        return new ProjectedPoint(screenX, screenY, true);
    }

    private static final class ProjectedPoint {
        private final int x;
        private final int y;
        private final boolean visible;

        private ProjectedPoint(int x, int y, boolean visible) {
            this.x = x;
            this.y = y;
            this.visible = visible;
        }

        private static ProjectedPoint hidden() {
            return new ProjectedPoint(0, 0, false);
        }
    }
}
