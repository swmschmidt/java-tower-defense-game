package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapContentLoaderTest {

    @Test
    void loadsPathAndCellsFromProperties(@TempDir Path tempDir) throws IOException {
        Path mapFile = tempDir.resolve("map.properties");
        Files.writeString(
            mapFile,
            """
            map.id=test-map
            map.path.waypoints=0.0,0.0,0.0;5.0,0.0,0.0;5.0,0.0,5.0
            map.buildable.cells=1:1;2:1
            map.blocked.cells=3:2
            """
        );

        GameplayMap map = new MapContentLoader().load(mapFile);

        assertEquals("test-map", map.id());
        assertEquals(3, map.path().waypoints().size());
        assertEquals(2, map.buildableCells().size());
        assertEquals(1, map.blockedCells().size());
        assertTrue(map.path().totalLength() > 0.0);
    }
}
