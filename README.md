# Java Tower Defense Game

Incremental playable foundation for a 3D tower defense game in Java 25.

## Current Step Scope (Step 02)

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
- Data-driven enemy definitions from content (`content/base/enemies/*.properties`)
- Deterministic enemy spawning and movement along map path waypoints
- Placeholder enemy rendering and map debug overlays (path, buildable and blocked cells)
- Lose trigger when an enemy reaches the goal

Still intentionally out of scope: tower combat, economy, selection UI, builder commands, upgrades, audio, and networking.

## Run

Prerequisites:

- JDK 25
- Maven 3.9+

Commands:

```bash
mvn test
mvn exec:java
```

Controls:

- `Esc`: close the game
- Window close button: close the game

Gameplay note:

- Enemies automatically spawn and walk along the configured map path.
- The match enters defeat state when any enemy reaches the path end.

## Config

Default config file:

- `src/main/resources/config/app.properties`

Optional local override file:

- `config/app.properties`

Any keys present in the local override file replace defaults.

Key content/config properties for this step:

- `content.map_file`
- `content.enemies_file`
- `gameplay.spawn.enemy_id`
- `gameplay.spawn.interval_seconds`
- `gameplay.spawn.max_count`

## Content Bootstrap

Root content folders:

- `content/base/`
- `content/mods/`

Current content files used by gameplay:

- `content/base/maps/sandbox-map.properties`
- `content/base/enemies/base-enemies.properties`
