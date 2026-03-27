# Java Tower Defense Game

Incremental playable foundation for a 3D tower defense game in Java 25.

## Current Step Scope (Step 06)

The project now provides:

- Maven project bootstrap targeting Java 25
- Main app entry point
- Fixed-timestep game loop with decoupled update and render phases
- Scene bootstrap for an initial sandbox world
- Renderer abstraction and software-rendered 3D debug grid
- Fixed camera suitable for tower-defense style overview
- Input abstraction with exit handling (`Esc`)
- Config loading structure (`src/main/resources/config/app.properties` + optional override file)
- Data-driven map loading from content (`content/base/maps/*.properties`)
- Data-driven enemy definitions from content (`content/base/enemies/*.properties`) including health and kill reward
- Data-driven tower definitions from content (`content/base/towers/*.properties`)
- Data-driven builder definitions from content (`content/base/units/*.properties`)
- Data-driven wave definitions from content (`content/base/waves/*.properties`)
- Wave spawner service driven by configured per-wave spawn groups
- Centralized match state machine with `PRE_WAVE`, `IN_WAVE`, `POST_WAVE`, `VICTORY`, and `DEFEAT`
- Basic tower placement (`T`) on buildable cells
- Selection system for world entities (builder and tower hit test hooks)
- Runtime builder unit instance with data-driven spawn and movement speed
- Right-click movement through a command queue (no direct input-to-gameplay mutation)
- Serializable-friendly command abstractions for selection and builder movement
- World picking infrastructure (screen-to-ground ray pick)
- Permanent lower HUD with three sections (minimap placeholder, selected-unit panel, action panel)
- Action menu buttons rendered from data-driven action definitions
- Hotkey binding support for action dispatch
- HUD button and hotkey dispatch routed into command flow (`UiActionCommand`)
- Target acquisition and attack cadence for placed towers
- Extensible attack abstraction with initial `hitscan` mode
- Enemy health, death handling, and gold reward on kill
- Economy and survival tracking with gold and lives
- Top status HUD plus permanent RTS-style lower HUD
- Placeholder rendering for towers, enemies, and map debug overlays (path, buildable and blocked cells)
- Builder placeholder rendering with selection ring visualization
- Win condition when all waves are cleared
- Lose trigger when player lives reach zero

Still intentionally out of scope: full minimap features, build placement flow, selling, upgrades, audio, and networking.

## Run

Prerequisites:

- JDK 25
- Maven 3.9+

Commands:

```bash
mvn test
mvn compile exec:java
```

Controls:

- `Esc`: close the game
- `T`: place one default tower on the next available buildable cell
- `Left Click`: select world entity
- `Right Click`: issue move command for selected builder
- `M`, `B`, `C`: action hotkeys (data-driven from UI action content)
- Window close button: close the game

Gameplay note:

- Enemies spawn according to configured wave definitions and walk along the configured map path.
- Placed towers automatically attack enemies in range.
- Player gains gold when towers kill enemies.
- Player loses 1 life for each enemy that reaches the goal.
- The match enters defeat state when lives reach zero.
- The match enters victory state after the final wave is fully cleared.

## Config

Default config file:

- `src/main/resources/config/app.properties`

Optional local override file:

- `config/app.properties`

Any keys present in the local override file replace defaults.

Key content/config properties for this step:

- `content.map_file`
- `content.enemies_file`
- `content.towers_file`
- `content.builders_file`
- `content.ui_actions_file`
- `content.waves_file`
- `gameplay.pre_wave_delay_seconds`
- `gameplay.post_wave_delay_seconds`
- `gameplay.default_tower_id`
- `gameplay.default_builder_id`
- `gameplay.default_hud_action_id`
- `gameplay.starting_gold`
- `gameplay.starting_lives`

## Content Bootstrap

Root content folders:

- `content/base/`
- `content/mods/`

Current content files used by gameplay:

- `content/base/maps/sandbox-map.properties`
- `content/base/enemies/base-enemies.properties`
- `content/base/towers/base-towers.properties`
- `content/base/units/base-builders.properties`
- `content/base/ui/base-actions.properties`
- `content/base/waves/sandbox-waves.properties`
