package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.tower.TowerCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TowerContentLoaderTest {

    @Test
    void loadsTowerDefinitionsFromProperties(@TempDir Path tempDir) throws IOException {
        Path towersFile = tempDir.resolve("towers.properties");
        Files.writeString(
            towersFile,
            """
            tower.ids=arrow;cannon
            tower.arrow.range_units=3.0
            tower.arrow.damage_per_shot=4.0
            tower.arrow.attacks_per_second=2.0
            tower.arrow.cost_gold=8
            tower.arrow.attack_mode=hitscan
            tower.cannon.range_units=2.2
            tower.cannon.damage_per_shot=10.0
            tower.cannon.attacks_per_second=0.7
            tower.cannon.cost_gold=18
            tower.cannon.attack_mode=hitscan
            """
        );

        TowerCatalog catalog = new TowerContentLoader().load(towersFile);

        assertEquals(2, catalog.definitionsById().size());
        assertEquals(2.2, catalog.required("cannon").rangeUnits());
        assertEquals(4.0, catalog.required("arrow").damagePerShot());
        assertEquals("hitscan", catalog.required("arrow").attackMode());
    }
}
