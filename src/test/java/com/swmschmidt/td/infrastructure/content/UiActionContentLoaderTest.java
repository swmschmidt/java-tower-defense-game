package com.swmschmidt.td.infrastructure.content;

import com.swmschmidt.td.core.gameplay.uiaction.UiActionCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiActionContentLoaderTest {

    @Test
    void loadsUiActionDefinitionsFromProperties(@TempDir Path tempDir) throws IOException {
        Path uiActionsFile = tempDir.resolve("ui-actions.properties");
        Files.writeString(
            uiActionsFile,
            """
            ui.action.ids=move;build_arrow;sell
            ui.action.move.label=Move
            ui.action.move.hotkey=M
            ui.action.move.entity_types=builder
            ui.action.move.mode=move
            ui.action.build_arrow.label=Build Arrow
            ui.action.build_arrow.hotkey=B
            ui.action.build_arrow.entity_types=builder
            ui.action.build_arrow.mode=build
            ui.action.build_arrow.tower_id=arrow
            ui.action.sell.label=Sell
            ui.action.sell.hotkey=X
            ui.action.sell.entity_types=tower
            ui.action.sell.mode=sell
            """
        );

        UiActionCatalog catalog = new UiActionContentLoader().load(uiActionsFile);

        assertEquals(2, catalog.actionsForEntityType("builder").size());
        assertEquals(1, catalog.actionsForEntityType("tower").size());
        assertEquals("move", catalog.resolveActionIdForHotkey("builder", "M"));
        assertEquals("build_arrow", catalog.resolveActionIdForHotkey("builder", "B"));
    }
}
