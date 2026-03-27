package com.swmschmidt.td.infrastructure.rendering.swing;

import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.EnemyView;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.MapDebugView;
import com.swmschmidt.td.core.scene.TowerView;
import com.swmschmidt.td.core.scene.WorldView;
import com.swmschmidt.td.infrastructure.rendering.api.FrameRenderer;
import com.swmschmidt.td.infrastructure.rendering.camera.FixedCamera;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

public final class SoftwareGridRenderer implements FrameRenderer {
    private static final double NEAR_PLANE = 0.1;

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
        drawEnemies(graphics, worldView.enemies(), camera, width, height);
        drawHud(
            graphics,
            worldView.currentWave(),
            worldView.totalWaves(),
            worldView.matchState(),
            worldView.playerGold(),
            worldView.playerLives()
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

    private void drawHud(Graphics2D graphics, int currentWave, int totalWaves, String matchState, int gold, int lives) {
        graphics.setColor(new Color(16, 20, 30, 170));
        graphics.fillRoundRect(14, 14, 340, 112, 12, 12);
        graphics.setColor(new Color(224, 228, 235));
        graphics.setFont(graphics.getFont().deriveFont(16f));
        graphics.drawString("Wave: " + currentWave + " / " + totalWaves, 24, 38);
        graphics.drawString("State: " + matchState, 24, 58);
        graphics.drawString("Gold: " + gold, 24, 78);
        graphics.drawString("Lives: " + lives, 24, 98);
        graphics.setFont(graphics.getFont().deriveFont(14f));
        graphics.drawString("Press T to place tower", 186, 98);
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
