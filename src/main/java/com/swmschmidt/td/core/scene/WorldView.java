package com.swmschmidt.td.core.scene;

import java.util.List;

public record WorldView(
	GridDefinition grid,
	MapDebugView mapDebugView,
	BuildPreviewView buildPreview,
	List<TowerView> towers,
	List<BuilderView> builders,
	List<EnemyView> enemies,
	List<HudActionView> hudActions,
	String activeHudActionId,
	String actionFeedbackMessage,
	String selectedEntityType,
	String selectedEntityId,
	int currentWave,
	int totalWaves,
	String matchState,
	int playerGold,
	int playerLives,
	boolean defeatTriggered,
	boolean victoryTriggered
) {
	public WorldView {
		towers = List.copyOf(towers);
		builders = List.copyOf(builders);
		enemies = List.copyOf(enemies);
		hudActions = List.copyOf(hudActions);
	}

	public WorldView(GridDefinition grid) {
		this(grid, null, null, List.of(), List.of(), List.of(), List.of(), "", "", "", "", 1, 1, "PRE_WAVE", 0, 0, false, false);
	}
}
