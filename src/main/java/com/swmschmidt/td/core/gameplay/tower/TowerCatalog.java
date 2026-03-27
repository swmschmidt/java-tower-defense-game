package com.swmschmidt.td.core.gameplay.tower;

import java.util.Map;

public final class TowerCatalog {
    private final Map<String, TowerDefinition> definitionsById;

    public TowerCatalog(Map<String, TowerDefinition> definitionsById) {
        if (definitionsById == null || definitionsById.isEmpty()) {
            throw new IllegalArgumentException("Tower catalog must contain at least one definition");
        }
        this.definitionsById = Map.copyOf(definitionsById);
    }

    public TowerDefinition required(String id) {
        TowerDefinition definition = definitionsById.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Missing tower definition: " + id);
        }
        return definition;
    }

    public Map<String, TowerDefinition> definitionsById() {
        return definitionsById;
    }
}
