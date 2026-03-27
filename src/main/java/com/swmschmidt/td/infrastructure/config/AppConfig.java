package com.swmschmidt.td.infrastructure.config;

import com.swmschmidt.td.core.math.Vector3;

public record AppConfig(
    String title,
    int windowWidth,
    int windowHeight,
    int updatesPerSecond,
    int framesPerSecond,
    Vector3 cameraPosition,
    Vector3 cameraTarget,
    double cameraFovDegrees,
    int gridHalfSize,
    double gridCellSize,
    String mapContentPath,
    String enemyContentPath,
    String towerContentPath,
    String builderContentPath,
    String uiActionContentPath,
    String waveContentPath,
    double preWaveDelaySeconds,
    double postWaveDelaySeconds,
    String defaultTowerId,
    String defaultBuilderId,
    String defaultHudActionId,
    int startingGold,
    int startingLives
) {
}
