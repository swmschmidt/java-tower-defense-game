package com.swmschmidt.td.core.gameplay.combat;

import com.swmschmidt.td.core.gameplay.enemy.EnemyInstance;
import com.swmschmidt.td.core.gameplay.tower.TowerInstance;

public final class HitscanAttackResolver implements AttackResolver {
    @Override
    public String attackModeId() {
        return "hitscan";
    }

    @Override
    public void resolve(TowerInstance tower, EnemyInstance target) {
        target.applyDamage(tower.definition().damagePerShot());
    }
}
