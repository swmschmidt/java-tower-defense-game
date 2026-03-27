# Project Structure

```text
.
|-- .github/
|   `-- workflows/
|       `-- ci.yml
|-- content/
|   |-- base/
|   |   |-- animations/
|   |   |-- audio/
|   |   |-- builders/
|   |   |-- enemies/
|   |   |   `-- base-enemies.properties
|   |   |-- factions/
|   |   |-- maps/
|   |   |   `-- sandbox-map.properties
|   |   |-- models/
|   |   |-- projectiles/
|   |   |-- textures/
|   |   |-- towers/
|   |   |-- ui/
|   |   |-- units/
|   |   |-- upgrades/
|   |   `-- waves/
|   `-- mods/
|       `-- example-mod/
|-- src/
|   |-- main/
|   |   |-- java/com/swmschmidt/td/
|   |   |   |-- application/scene/
|   |   |   |-- bootstrap/
|   |   |   |-- core/
|   |   |   |   |-- gameplay/
|   |   |   |   |   |-- enemy/
|   |   |   |   |   `-- map/
|   |   |   |   |-- gameloop/
|   |   |   |   |-- input/
|   |   |   |   |-- math/
|   |   |   |   `-- scene/
|   |   |   `-- infrastructure/
|   |   |       |-- config/
|   |   |       |-- content/
|   |   |       |-- input/
|   |   |       `-- rendering/
|   |   |           |-- api/
|   |   |           |-- camera/
|   |   |           `-- swing/
|   |   `-- resources/config/
|   `-- test/java/com/swmschmidt/td/
|       |-- application/scene/
|       |-- core/gameloop/
|       `-- infrastructure/
|           |-- config/
|           `-- content/
|-- .gitignore
|-- README.md
|-- PROJECT_STRUCTURE.md
|-- pom.xml
`-- tower-defense-incremental-plan.md
```

## Notes

- The current implementation targets Step 02.
- Rendering is isolated behind `FrameRenderer`.
- Scene state is exposed through `WorldView` to keep simulation decoupled from rendering.
- Map and enemy definitions are loaded from external content files.
