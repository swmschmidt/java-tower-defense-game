package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.wave.WaveCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaveContentLoaderTest {

    @Test
    void loadsWaveDefinitionsFromProperties(@TempDir Path tempDir) throws IOException {
        Path waveFile = tempDir.resolve("waves.properties");
        Files.writeString(
            waveFile,
            """
            wave.ids=wave_01;wave_02
            wave.wave_01.spawn.ids=s1
            wave.wave_01.spawn.s1.enemy_id=grunt
            wave.wave_01.spawn.s1.count=5
            wave.wave_01.spawn.s1.interval_seconds=1.2
            wave.wave_02.spawn.ids=s1;s2
            wave.wave_02.spawn.s1.enemy_id=grunt
            wave.wave_02.spawn.s1.count=3
            wave.wave_02.spawn.s1.interval_seconds=0.9
            wave.wave_02.spawn.s2.enemy_id=runner
            wave.wave_02.spawn.s2.count=2
            wave.wave_02.spawn.s2.interval_seconds=0.6
            """
        );

        WaveCatalog catalog = new WaveContentLoader().load(waveFile);

        assertEquals(2, catalog.totalWaves());
        assertEquals("wave_01", catalog.waveAt(0).id());
        assertEquals(5, catalog.waveAt(0).spawns().getFirst().count());
        assertEquals("runner", catalog.waveAt(1).spawns().get(1).enemyId());
        assertEquals(0.6, catalog.waveAt(1).spawns().get(1).intervalSeconds());
    }
}
