package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.map.GameplayMap;
import com.swmschmidt.td.core.gameplay.map.GridCell;
import com.swmschmidt.td.core.gameplay.map.MapPath;
import com.swmschmidt.td.core.math.Vector3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public final class MapContentLoader {

    public GameplayMap load(Path filePath) {
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(filePath)) {
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load map content from " + filePath, exception);
        }

        String mapId = required(properties, "map.id");
        List<Vector3> waypoints = parseWaypoints(required(properties, "map.path.waypoints"));

        Set<GridCell> buildableCells = parseCells(properties.getProperty("map.buildable.cells", ""));
        Set<GridCell> blockedCells = parseCells(properties.getProperty("map.blocked.cells", ""));

        return new GameplayMap(mapId, new MapPath(waypoints), buildableCells, blockedCells);
    }

    private List<Vector3> parseWaypoints(String rawValue) {
        String[] tokens = rawValue.split(";");
        if (tokens.length < 2) {
            throw new IllegalStateException("map.path.waypoints must contain at least two waypoints");
        }

        return List.of(tokens).stream()
            .map(String::trim)
            .filter(token -> !token.isBlank())
            .map(this::parseVector)
            .toList();
    }

    private Vector3 parseVector(String token) {
        String[] parts = token.split(",");
        if (parts.length != 3) {
            throw new IllegalStateException("Waypoint must be x,y,z but got: " + token);
        }
        return new Vector3(
            Double.parseDouble(parts[0].trim()),
            Double.parseDouble(parts[1].trim()),
            Double.parseDouble(parts[2].trim())
        );
    }

    private Set<GridCell> parseCells(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Set.of();
        }

        Set<GridCell> cells = new LinkedHashSet<>();
        for (String token : rawValue.split(";")) {
            String trimmed = token.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            String[] coordinates = trimmed.split(":");
            if (coordinates.length != 2) {
                throw new IllegalStateException("Cell must be x:z but got: " + trimmed);
            }
            cells.add(new GridCell(
                Integer.parseInt(coordinates[0].trim()),
                Integer.parseInt(coordinates[1].trim())
            ));
        }
        return Set.copyOf(cells);
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required map property: " + key);
        }
        return value;
    }
}
