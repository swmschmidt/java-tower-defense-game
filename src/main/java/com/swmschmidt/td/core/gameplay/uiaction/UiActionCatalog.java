package com.swmschmidt.td.core.gameplay.uiaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UiActionCatalog {
    private final List<UiActionDefinition> orderedDefinitions;
    private final Map<String, UiActionDefinition> definitionsById;

    public UiActionCatalog(List<UiActionDefinition> orderedDefinitions) {
        if (orderedDefinitions == null || orderedDefinitions.isEmpty()) {
            throw new IllegalArgumentException("UI action catalog must contain at least one definition");
        }
        List<UiActionDefinition> copy = List.copyOf(orderedDefinitions);
        Map<String, UiActionDefinition> byId = new LinkedHashMap<>();
        for (UiActionDefinition definition : copy) {
            UiActionDefinition previous = byId.put(definition.id(), definition);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate UI action id: " + definition.id());
            }
        }
        this.orderedDefinitions = copy;
        this.definitionsById = Map.copyOf(byId);
    }

    public UiActionDefinition required(String id) {
        UiActionDefinition definition = definitionsById.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Missing UI action definition: " + id);
        }
        return definition;
    }

    public List<UiActionDefinition> actionsForEntityType(String entityType) {
        String resolvedType = (entityType == null || entityType.isBlank()) ? "none" : entityType.trim().toLowerCase();
        List<UiActionDefinition> result = new ArrayList<>();
        for (UiActionDefinition definition : orderedDefinitions) {
            if (definition.supportedEntityTypes().contains(resolvedType)) {
                result.add(definition);
            }
        }
        return result;
    }

    public String resolveActionIdForHotkey(String entityType, String hotkey) {
        if (hotkey == null || hotkey.isBlank()) {
            return null;
        }
        String normalized = hotkey.trim().toUpperCase();
        for (UiActionDefinition definition : actionsForEntityType(entityType)) {
            if (definition.hotkey().equals(normalized)) {
                return definition.id();
            }
        }
        return null;
    }
}
