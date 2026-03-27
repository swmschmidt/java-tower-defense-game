package com.swmschmidt.td.core.scene;

import com.swmschmidt.td.core.math.Vector3;

public record TowerView(
	String instanceId,
	String towerId,
	Vector3 position,
	double rangeUnits,
	boolean selected
) {
}
