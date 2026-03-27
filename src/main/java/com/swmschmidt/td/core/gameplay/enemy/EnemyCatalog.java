package com.swmschmidt.td.core.gameplay.enemy;

import java.util.Map;

public final class EnemyCatalog {
    private final Map<String, EnemyDefinition> definitionsById;

    public EnemyCatalog(Map<String, EnemyDefinition> definitionsById) {
        if (definitionsById == null || definitionsById.isEmpty()) {
            throw new IllegalArgumentException("Enemy catalog must contain at least one definition");
        }
        this.definitionsById = Map.copyOf(definitionsById);
    }

    public EnemyDefinition required(String id) {
        EnemyDefinition definition = definitionsById.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Missing enemy definition: " + id);
        }
        return definition;
    }

    public Map<String, EnemyDefinition> definitionsById() {
        return definitionsById;
    }
}
