package com.swmschmidt.td.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigLoaderTest {

    @Test
    void loadsDefaultConfigFromClasspath() {
        AppConfig config = new AppConfigLoader().load();

        assertEquals("Java Tower Defense Sandbox", config.title());
        assertEquals(1280, config.windowWidth());
        assertEquals(720, config.windowHeight());
        assertEquals(60, config.updatesPerSecond());
        assertTrue(config.gridHalfSize() > 0);
        assertTrue(config.gridCellSize() > 0.0);
        assertTrue(!config.mapContentPath().isBlank());
        assertTrue(!config.enemyContentPath().isBlank());
        assertTrue(!config.towerContentPath().isBlank());
        assertTrue(!config.builderContentPath().isBlank());
        assertTrue(!config.waveContentPath().isBlank());
        assertTrue(config.preWaveDelaySeconds() >= 0.0);
        assertTrue(config.postWaveDelaySeconds() >= 0.0);
        assertTrue(!config.defaultTowerId().isBlank());
        assertTrue(!config.defaultBuilderId().isBlank());
        assertTrue(config.startingGold() >= 0);
        assertTrue(config.startingLives() > 0);
    }
}
