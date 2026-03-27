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
|   |   |   `-- base-towers.properties
|   |   |-- ui/
|   |   |-- units/
|   |   |-- upgrades/
|   |   `-- waves/
|   |   |   `-- sandbox-waves.properties
|   `-- mods/
|       `-- example-mod/
|-- src/
|   |-- main/
|   |   |-- java/com/swmschmidt/td/
|   |   |   |-- application/scene/
|   |   |   |-- bootstrap/
|   |   |   |-- core/
|   |   |   |   |-- gameplay/
|   |   |   |   |   |-- builder/
|   |   |   |   |   |-- combat/
|   |   |   |   |   |-- command/
|   |   |   |   |   |-- enemy/
|   |   |   |   |   |-- match/
|   |   |   |   |   |-- uiaction/
|   |   |   |   |   `-- map/
|   |   |   |   |   `-- tower/
|   |   |   |   |   `-- wave/
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

- The current implementation targets Step 07.
- Gameplay simulation is decoupled from rendering using `WorldView` snapshot pattern.
- Rendering is isolated behind `FrameRenderer`.
- Input is routed to a command queue; no direct input-to-gameplay mutations occur.
- Map, enemy, tower, builder, UI action, and wave definitions are loaded from external content files.
- Match flow is centralized in gameplay state components (`match` and `wave` packages).
- Builder actions (move, build, sell) use config-driven mode metadata with optional per-action tower binding.
- Tower instance IDs enable runtime tracking for selection, selling, and targeting.
- Build preview state provides placement feedback (valid/invalid).
- Sell refund ratios are per-tower and data-driven.
- Lower HUD rendering and hit-testing are isolated in presentation classes (`SoftwareGridRenderer`, `LowerHudLayout`).
- Scene state exposure through `WorldView` keeps simulation fully decoupled from rendering.
