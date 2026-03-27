package com.swmschmidt.td.core.gameplay.combat;

import com.swmschmidt.td.core.gameplay.enemy.EnemyInstance;
import com.swmschmidt.td.core.gameplay.tower.TowerInstance;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TowerCombatSystem {
    private final Map<String, AttackResolver> attackResolvers;

    public TowerCombatSystem(List<AttackResolver> attackResolvers) {
        if (attackResolvers == null || attackResolvers.isEmpty()) {
            throw new IllegalArgumentException("At least one attack resolver is required");
        }
        this.attackResolvers = attackResolvers.stream()
            .collect(Collectors.toUnmodifiableMap(AttackResolver::attackModeId, Function.identity()));
    }

    public void update(List<TowerInstance> towers, List<EnemyInstance> enemies, double deltaSeconds) {
        for (TowerInstance tower : towers) {
            tower.tickCooldown(deltaSeconds);
            if (!tower.isReadyToAttack()) {
                continue;
            }

            EnemyInstance target = acquireTarget(tower, enemies);
            if (target == null) {
                continue;
            }

            AttackResolver resolver = attackResolvers.get(tower.definition().attackMode());
            if (resolver == null) {
                throw new IllegalStateException("No attack resolver for mode: " + tower.definition().attackMode());
            }

            resolver.resolve(tower, target);
            tower.consumeAttack();
        }
    }

    private EnemyInstance acquireTarget(TowerInstance tower, List<EnemyInstance> enemies) {
        EnemyInstance best = null;
        double bestDistanceSq = Double.MAX_VALUE;
        double maxRange = tower.definition().rangeUnits();
        double maxRangeSq = maxRange * maxRange;

        for (EnemyInstance enemy : enemies) {
            if (!enemy.isAlive() || enemy.reachedGoal()) {
                continue;
            }

            double dx = enemy.position().x() - tower.position().x();
            double dz = enemy.position().z() - tower.position().z();
            double distanceSq = (dx * dx) + (dz * dz);
            if (distanceSq > maxRangeSq) {
                continue;
            }

            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                best = enemy;
            }
        }

        return best;
    }
}
