package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.builder.BuilderCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuilderContentLoaderTest {

    @Test
    void loadsBuilderDefinitionsFromProperties(@TempDir Path tempDir) throws IOException {
        Path buildersFile = tempDir.resolve("builders.properties");
        Files.writeString(
            buildersFile,
            """
            builder.ids=builder;worker
            builder.builder.move_speed_units_per_second=4.0
            builder.builder.selection_radius=0.4
            builder.builder.build_range_units=3.5
            builder.builder.spawn_x=-12.0
            builder.builder.spawn_z=-10.0
            builder.worker.move_speed_units_per_second=3.2
            builder.worker.selection_radius=0.35
            builder.worker.build_range_units=2.2
            builder.worker.spawn_x=2.0
            builder.worker.spawn_z=3.0
            """
        );

        BuilderCatalog catalog = new BuilderContentLoader().load(buildersFile);

        assertEquals(2, catalog.definitionsById().size());
        assertEquals(3.2, catalog.required("worker").moveSpeedUnitsPerSecond());
        assertEquals(-10.0, catalog.required("builder").spawnZ());
        assertEquals(2.2, catalog.required("worker").buildRangeUnits());
    }
}
