package com.swmschmidt.td.core.gameplay.builder;

import java.util.Map;

public final class BuilderCatalog {
    private final Map<String, BuilderDefinition> definitionsById;

    public BuilderCatalog(Map<String, BuilderDefinition> definitionsById) {
        if (definitionsById == null || definitionsById.isEmpty()) {
            throw new IllegalArgumentException("Builder catalog must contain at least one definition");
        }
        this.definitionsById = Map.copyOf(definitionsById);
    }

    public BuilderDefinition required(String id) {
        BuilderDefinition definition = definitionsById.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Missing builder definition: " + id);
        }
        return definition;
    }

    public Map<String, BuilderDefinition> definitionsById() {
        return definitionsById;
    }
}
