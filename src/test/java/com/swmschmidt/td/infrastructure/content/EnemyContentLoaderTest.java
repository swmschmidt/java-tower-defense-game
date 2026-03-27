package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.enemy.EnemyCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnemyContentLoaderTest {

    @Test
    void loadsEnemyDefinitionsFromProperties(@TempDir Path tempDir) throws IOException {
        Path enemyFile = tempDir.resolve("enemies.properties");
        Files.writeString(
            enemyFile,
            """
            enemy.ids=grunt;runner
            enemy.grunt.speed_units_per_second=1.5
            enemy.grunt.radius=0.3
            enemy.runner.speed_units_per_second=2.7
            enemy.runner.radius=0.25
            """
        );

        EnemyCatalog catalog = new EnemyContentLoader().load(enemyFile);

        assertEquals(2, catalog.definitionsById().size());
        assertEquals(2.7, catalog.required("runner").speedUnitsPerSecond());
        assertEquals(0.3, catalog.required("grunt").radius());
    }
}
