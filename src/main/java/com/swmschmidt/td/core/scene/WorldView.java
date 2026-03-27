package com.swmschmidt.td.core.scene;

import java.util.List;

public record WorldView(
	GridDefinition grid,
	MapDebugView mapDebugView,
	List<TowerView> towers,
	List<EnemyView> enemies,
	int playerGold,
	int playerLives,
	boolean defeatTriggered
) {
	public WorldView {
		towers = List.copyOf(towers);
		enemies = List.copyOf(enemies);
	}

	public WorldView(GridDefinition grid) {
		this(grid, null, List.of(), List.of(), 0, 0, false);
	}
}
