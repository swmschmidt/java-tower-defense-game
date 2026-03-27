# Java Tower Defense Game

Initial playable foundation for a 3D tower defense game in Java 25.

## Step 01 Scope

This baseline provides:

- Maven project bootstrap targeting Java 25
- Main app entry point
- Fixed-timestep game loop with decoupled update and render phases
- Scene bootstrap for an initial sandbox world
- Renderer abstraction and software-rendered 3D debug grid
- Fixed camera suitable for tower-defense style overview
- Input abstraction with exit handling (`Esc`)
- Config loading structure (`src/main/resources/config/app.properties` + optional override file)
- Content directory bootstrap for future data-driven systems

No gameplay systems are implemented yet (no towers, enemies, waves, UI panels, audio, or networking).

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

## Config

Default config file:

- `src/main/resources/config/app.properties`

Optional local override file:

- `config/app.properties`

Any keys present in the local override file replace defaults.

## Content Bootstrap

Root content folders:

- `content/base/`
- `content/mods/`

These directories are intentionally created early so upcoming gameplay steps can be content-driven.
