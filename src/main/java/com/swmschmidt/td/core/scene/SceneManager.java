package com.swmschmidt.td.core.scene;

public final class SceneManager {
    private Scene activeScene;

    public SceneManager(Scene initialScene) {
        this.activeScene = initialScene;
    }

    public void update(double deltaSeconds) {
        activeScene.update(deltaSeconds);
    }

    public WorldView captureView() {
        return activeScene.captureView();
    }

    public void setActiveScene(Scene scene) {
        this.activeScene = scene;
    }
}
