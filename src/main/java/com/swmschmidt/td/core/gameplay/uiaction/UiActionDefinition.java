package com.swmschmidt.td.core.gameplay.uiaction;

import java.util.LinkedHashSet;
import java.util.Set;

public record UiActionDefinition(
    String id,
    String label,
    String hotkey,
    Set<String> supportedEntityTypes,
    UiActionMode mode,
    String towerId
) {
    public UiActionDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("UI action id must not be blank");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("UI action label must not be blank");
        }
        if (hotkey == null || hotkey.isBlank()) {
            throw new IllegalArgumentException("UI action hotkey must not be blank");
        }
        hotkey = hotkey.trim().toUpperCase();
        if (hotkey.length() != 1) {
            throw new IllegalArgumentException("UI action hotkey must be exactly one character");
        }
        if (supportedEntityTypes == null || supportedEntityTypes.isEmpty()) {
            throw new IllegalArgumentException("UI action supportedEntityTypes must not be empty");
        }
        if (mode == null) {
            throw new IllegalArgumentException("UI action mode must not be null");
        }
        Set<String> normalizedEntityTypes = new LinkedHashSet<>();
        for (String value : supportedEntityTypes) {
            if (value == null || value.isBlank()) {
                continue;
            }
            normalizedEntityTypes.add(value.trim().toLowerCase());
        }
        if (normalizedEntityTypes.isEmpty()) {
            throw new IllegalArgumentException("UI action supportedEntityTypes must include at least one value");
        }
        if (mode == UiActionMode.BUILD && (towerId == null || towerId.isBlank())) {
            throw new IllegalArgumentException("Build action must define towerId");
        }
        if (mode != UiActionMode.BUILD) {
            towerId = "";
        }
        supportedEntityTypes = Set.copyOf(normalizedEntityTypes);
    }
}
