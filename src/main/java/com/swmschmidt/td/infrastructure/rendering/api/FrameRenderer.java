package com.swmschmidt.td.infrastructure.rendering.api;

import com.swmschmidt.td.core.scene.WorldView;
import com.swmschmidt.td.infrastructure.rendering.camera.FixedCamera;

import java.awt.image.BufferedImage;

public interface FrameRenderer {
    BufferedImage render(WorldView worldView, FixedCamera camera, int width, int height);
}
