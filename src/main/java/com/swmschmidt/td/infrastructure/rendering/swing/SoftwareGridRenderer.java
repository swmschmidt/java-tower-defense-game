package com.swmschmidt.td.infrastructure.rendering.swing;

import com.swmschmidt.td.core.math.Vector3;
import com.swmschmidt.td.core.scene.GridDefinition;
import com.swmschmidt.td.core.scene.WorldView;
import com.swmschmidt.td.infrastructure.rendering.api.FrameRenderer;
import com.swmschmidt.td.infrastructure.rendering.camera.FixedCamera;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class SoftwareGridRenderer implements FrameRenderer {
    private static final double NEAR_PLANE = 0.1;

    @Override
    public BufferedImage render(WorldView worldView, FixedCamera camera, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(graphics, width, height);
        drawGroundGrid(graphics, worldView.grid(), camera, width, height);
        graphics.dispose();
        return image;
    }

    private void drawBackground(Graphics2D graphics, int width, int height) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(35, 47, 74), 0, height, new Color(17, 22, 33));
        graphics.setPaint(sky);
        graphics.fillRect(0, 0, width, height);
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
