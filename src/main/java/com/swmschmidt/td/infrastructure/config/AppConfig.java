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
    String spawnEnemyId,
    double spawnIntervalSeconds,
    int spawnMaxCount,
    String defaultTowerId,
    int startingGold,
    int startingLives
) {
}
