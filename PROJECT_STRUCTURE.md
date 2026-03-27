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
|   |   |-- factions/
|   |   |-- maps/
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
|   |   |   |   |-- gameloop/
|   |   |   |   |-- input/
|   |   |   |   |-- math/
|   |   |   |   `-- scene/
|   |   |   `-- infrastructure/
|   |   |       |-- config/
|   |   |       |-- input/
|   |   |       `-- rendering/
|   |   |           |-- api/
|   |   |           |-- camera/
|   |   |           `-- swing/
|   |   `-- resources/config/
|   `-- test/java/com/swmschmidt/td/
|       |-- core/gameloop/
|       `-- infrastructure/config/
|-- .gitignore
|-- README.md
|-- PROJECT_STRUCTURE.md
|-- pom.xml
`-- tower-defense-incremental-plan.md
```

## Notes

- The current implementation targets Step 01 only.
- Rendering is isolated behind `FrameRenderer`.
- Scene state is exposed through `WorldView` to keep simulation decoupled from rendering.
- Content and configuration structures are bootstrapped for future data-driven gameplay steps.
