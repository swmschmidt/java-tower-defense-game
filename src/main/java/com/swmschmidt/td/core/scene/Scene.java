package com.swmschmidt.td.core.scene;

public interface Scene {
    void update(double deltaSeconds);

    WorldView captureView();
}
