package com.swmschmidt.td.core.scene;

import java.util.List;

public record WorldView(
	GridDefinition grid,
	MapDebugView mapDebugView,
	List<EnemyView> enemies,
	boolean defeatTriggered
) {
	public WorldView {
		enemies = List.copyOf(enemies);
	}

	public WorldView(GridDefinition grid) {
		this(grid, null, List.of(), false);
	}
}
