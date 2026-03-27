package com.swmschmidt.td.core.gameplay.combat;

import com.swmschmidt.td.core.gameplay.enemy.EnemyInstance;
import com.swmschmidt.td.core.gameplay.tower.TowerInstance;

public interface AttackResolver {
    String attackModeId();

    void resolve(TowerInstance tower, EnemyInstance target);
}
