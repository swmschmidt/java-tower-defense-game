# Tower Defense Game in Java 25 - Incremental Development Plan

## Goals

Build a 3D tower defense game with a fixed camera, playable at every milestone, using Java 25 and a codebase that is simple, performant, easy to test, easy to mod, and easy to extend without rewriting existing systems. The architecture must favor composition, data-driven content, stable interfaces, separation of concerns, and future support for textures, animations, audio, upgrades, builder control, UI actions, and multiplayer.

This plan is written for human developers, but structured primarily to guide AI coding agents safely and incrementally.

## Core Development Principles

- Every step must end in a playable state.
- Prefer adding new modules over rewriting existing ones.
- Keep gameplay rules data-driven whenever practical.
- Keep rendering, simulation, input, UI, asset loading, networking, and content definitions separated.
- Do not hardcode tower stats, enemy stats, waves, hotkeys, or economy values in gameplay logic unless they are bootstrap defaults.
- Use plain, readable Java first. Introduce complexity only when the game genuinely needs it.
- Optimize from the start through good architecture, not premature micro-optimizations.
- Build for deterministic simulation as early as practical, because it helps replay support, testing, and multiplayer.
- Comments should be minimal and only where they clarify non-obvious intent.

## Recommended High-Level Tech Direction

This plan assumes:

- Java 25
- Gradle or Maven
- OpenGL-based rendering through a Java library such as LWJGL or an engine wrapper if needed
- JUnit for tests
- JSON, TOML, YAML, or similar config-driven content definitions

The exact rendering library can remain abstract in early steps, but the project structure must isolate the renderer behind interfaces so that rendering details never leak into domain logic.

## Architecture Targets

### Main layers

- `bootstrap`: app startup, dependency wiring, configuration bootstrap
- `core`: game loop, timing, scene lifecycle, shared abstractions
- `domain`: pure gameplay rules and entities
- `application`: orchestration use cases, commands, game services
- `infrastructure`: rendering, file IO, audio, config loading, networking
- `presentation`: HUD, menus, selection panels, minimap presentation, hotkeys
- `tools`: debug overlays, content validation, dev-only helpers

### Guiding patterns

- Data-driven definitions for units, towers, enemies, projectiles, waves, upgrades, sounds, UI actions
- ECS-like thinking where useful, but not necessarily a full ECS framework from day one
- Interfaces around rendering, input, audio, networking, content loading
- Stable event system for gameplay and UI communication
- Command system for player actions
- State machines for unit behavior and match flow
- Separate runtime instances from content definitions
- Content folders for mods and overrides

## Modding and Data-Driven Requirements

The game should be built so that changing stats, towers, wave composition, hotkeys, costs, build menus, sounds, and later even models or animations does not require gameplay code changes.

Plan for these content roots from the beginning:

```text
content/
  base/
    units/
    towers/
    enemies/
    projectiles/
    waves/
    upgrades/
    ui/
    audio/
    maps/
    factions/
    builders/
    textures/
    models/
    animations/
  mods/
    example-mod/
      ...
```

Content loading rules should support:

- Base content
- Optional mod content layered on top
- Validation with clear error messages
- IDs instead of hardcoded references

## Recommended Project Structure

```text
project-root/
  README.md
  PROJECT_STRUCTURE.md
  build.gradle or pom.xml
  settings.gradle
  gradle/
  src/
    main/
      java/
        com/example/td/
          bootstrap/
          core/
            gameloop/
            scene/
            time/
            math/
            event/
          domain/
            map/
            player/
            unit/
            tower/
            enemy/
            projectile/
            wave/
            economy/
            build/
            upgrade/
            combat/
            path/
            selection/
            command/
            match/
          application/
            service/
            usecase/
            command/
            query/
          infrastructure/
            config/
            filesystem/
            rendering/
              api/
              scene/
              mesh/
              camera/
              terrain/
              material/
              animation/
              debug/
            input/
            audio/
            networking/
            serialization/
          presentation/
            hud/
            menu/
            panel/
            minimap/
            cursor/
            hotkey/
            formatting/
          tools/
            validation/
            debug/
      resources/
        shaders/
        fonts/
    test/
      java/
        com/example/td/
          unit/
          integration/
          content/
  content/
    base/
    mods/
```

## Incremental Roadmap

---

## Step 01 - Bootstrap a playable 3D sandbox

### Objective
Create the minimal application shell with a game loop, fixed camera, 3D world rendering, simple map plane, input handling, update-render separation, and placeholder debug drawing.

### Playable result
The player can launch the game, see a 3D map from a fixed camera, move the cursor, and quit cleanly.

### Todo
- Create the project structure.
- Set up build, run, and test tasks.
- Add app bootstrap and dependency wiring.
- Create game loop with fixed timestep update and decoupled render.
- Add camera abstraction for fixed isometric or Warcraft 3 style view.
- Render a simple ground plane with grid or debug tiles.
- Add input service abstraction.
- Add basic scene system.
- Add config bootstrap for screen, camera, and debug settings.
- Add content directory bootstrap.
- Add README and Project Structure documentation.

### Design constraints
- No gameplay logic in renderer.
- No direct static access to input, rendering, or content.
- Even placeholder rendering must go through rendering interfaces.

---

## Step 02 - Deterministic map, lanes, and enemy pathing foundation

### Objective
Introduce a simulation-friendly map model, buildable and non-buildable cells, path definitions, and enemy route following using simple placeholders.

### Playable result
Enemies spawn automatically and walk along a fixed path to the exit while the player watches the route in 3D.

### Todo
- Create map domain model independent from rendering.
- Load map data from config files.
- Represent path nodes or nav path definitions from content files.
- Add enemy runtime entity with movement component/state.
- Add deterministic movement update using simulation time.
- Render enemies as simple primitives or placeholders.
- Add match lose condition trigger when enemies reach goal.
- Add debug overlay for path and cell occupancy.

### Design constraints
- Path data must be externalized.
- Movement logic must not depend on frame rate.
- Runtime enemy instances must reference immutable enemy definitions.

---

## Step 03 - Basic combat loop with one tower type

### Objective
Add the first complete gameplay loop: one buildable tower, autonomous targeting, projectile or instant-hit combat, enemy death, player lives, and simple gold reward.

### Playable result
The player can place a single tower type before or during the wave and watch it kill enemies.

### Todo
- Create tower definition and runtime tower entity split.
- Add target acquisition system.
- Add attack cadence logic.
- Add projectile system or simple hitscan abstraction with extensible interface.
- Add health component and damage resolution.
- Add enemy death events.
- Add gold rewards on kill.
- Add basic build validation on buildable cells.
- Add very simple build interaction, even if temporary.

### Design constraints
- Tower stats must come from config files.
- Combat systems must be isolated from UI and renderer.
- Damage and reward rules must be testable without graphics.

---

## Step 04 - Economy, match states, and real wave progression

### Objective
Replace ad hoc spawning with a proper wave system, match state flow, economy rules, and win or lose conditions.

### Playable result
The player can start waves, survive multiple waves, earn gold, spend gold, and eventually win or lose a simple match.

### Todo
- Add wave definitions loaded from files.
- Add pre-wave, in-wave, post-wave, victory, and defeat match states.
- Add wave spawner service driven by content definitions.
- Add player economy service with gold and lives.
- Add wave countdown timers.
- Add basic HUD text for lives, gold, wave, and state.
- Add simple game over and victory screens.

### Design constraints
- Wave behavior must be data-driven.
- Match state machine must be centralized.
- UI must only observe application state, not compute rules.

---

## Step 05 - Proper selection, command model, and builder unit foundation

### Objective
Introduce the selection system and the concept of a controllable builder unit. Start replacing temporary building shortcuts with explicit player commands.

### Playable result
The player can select a builder unit, move it, and prepare for context-based actions.

### Todo
- Add selection system for world entities.
- Add builder definition and runtime unit.
- Add right-click movement command.
- Add command queue or immediate command execution framework.
- Add ground picking or raycast selection abstraction.
- Add selection visuals and selected entity info panel stub.
- Add action command architecture for move, build, and sell.

### Design constraints
- Input should produce commands, not call gameplay logic directly.
- Builder must use the same unit architecture planned for future animated units.
- Commands should be network-friendly and serializable in the future.

---

## Step 06 - Lower HUD frame and Warcraft-style interaction layout

### Objective
Create the permanent lower-screen UI layout: minimap area, selected unit stats and portrait area, and action menu with clickable buttons and hotkeys.

### Playable result
The player can interact using a stable RTS-style lower HUD, with a visible selected-unit panel and command buttons.

### Todo
- Create lower HUD layout with three sections.
- Add minimap panel placeholder with map bounds and unit markers.
- Add selected entity panel with name, stats, placeholder image, and status.
- Add action menu panel with buttons.
- Add hotkey binding support from config.
- Add button command dispatch.
- Add hover, disabled, cooldown-ready, and selected states.
- Add input focus rules so UI and world input do not conflict.

### Design constraints
- UI layout must not contain gameplay rules.
- Action menu content should be driven by selected entity actions.
- Hotkeys should be externalized in config.

---

## Step 07 - Builder-driven tower construction and selling

### Objective
Implement the actual intended gameplay loop: move builder by right click, build towers through the UI, and sell towers through UI actions and hotkeys.

### Playable result
The player selects the builder, right-click moves, opens build actions, places towers legally, and can sell placed towers.

### Todo
- Add build menu definitions in config.
- Add build preview ghost rendering.
- Add build placement validation.
- Add builder build range or interaction rules.
- Add tower construction commands.
- Add selling rules and refund config.
- Add action menu states for build, cancel, move, sell.
- Add tower selection and sell action.
- Add failure feedback for invalid build attempts.

### Design constraints
- Buildable tower catalog must be content-driven.
- Selling rules must be data-driven.
- Builder action rules must not be embedded in UI widgets.

---

## Step 08 - More tower and enemy types through content only

### Objective
Prove that the architecture supports expansion without changing gameplay code by adding multiple towers and enemies mostly through content files.

### Playable result
The game includes several distinct enemy and tower types with different stats and behavior combinations.

### Todo
- Add multiple tower definitions.
- Add multiple enemy definitions.
- Add attack traits such as range, cooldown, projectile speed, splash flag, target priority.
- Add enemy traits such as speed, health, armor type placeholder, reward, size.
- Add resistances or tags abstraction if needed.
- Add UI representation for tower and enemy metadata.
- Add content validation tests.

### Design constraints
- Prefer generic behaviors configured by data over new per-unit classes.
- Only add new code where there is truly new behavior.

---

## Step 09 - Upgrades and tower progression

### Objective
Add a reusable upgrade system for towers that supports branching later, but starts simple.

### Playable result
The player can select a tower and buy upgrades from the action panel, affecting combat performance.

### Todo
- Add upgrade definitions in content.
- Add upgrade prerequisites and costs.
- Add runtime upgrade application rules.
- Add tower level and upgraded stats display.
- Add disabled-state reasons in UI.
- Add sell refund recalculation after upgrades.
- Add upgrade-related tests.

### Design constraints
- Upgrades must not mutate base definitions.
- Upgrade logic should support future branching paths without redesign.

---

## Step 10 - Audio system and event-driven sound playback

### Objective
Add sound support using an event-driven approach so audio remains decoupled from gameplay systems.

### Playable result
The game plays sounds for clicks, tower attacks, enemy death, wave start, invalid actions, and selection.

### Todo
- Add audio service abstraction.
- Add sound event mapping from content.
- Add volume categories such as master, UI, SFX, ambient.
- Add sound triggers from domain or application events.
- Add placeholder audio asset loading.
- Add mute and volume config.

### Design constraints
- Gameplay code must emit events, not play files directly.
- Audio mappings should be data-driven.

---

## Step 11 - Texture, model, and animation-ready rendering pipeline

### Objective
Refactor rendering only where necessary to support future real assets while keeping gameplay untouched.

### Playable result
The game still plays the same, but entities can now reference model, texture, and animation identifiers through content definitions.

### Todo
- Add asset reference fields to content schemas.
- Introduce render components or presentation descriptors.
- Add material and model loading interfaces.
- Add animation controller abstraction for units and towers.
- Keep fallback placeholders if assets are missing.
- Add portrait image references for UI.
- Add renderer resource cache.

### Design constraints
- Domain logic must remain unaware of model or animation implementation.
- Missing assets must fail gracefully.

---

## Step 12 - Minimap, fog hooks, and better RTS feedback

### Objective
Improve spatial readability and RTS feel without disrupting existing logic.

### Playable result
The minimap becomes genuinely useful, world interactions are clearer, and player feedback improves.

### Todo
- Upgrade minimap rendering with path, towers, enemies, builder, and camera bounds.
- Add optional minimap click or ping hooks.
- Add world decals or markers for commands.
- Add health bars, selection rings, and range preview.
- Add build placement overlays.
- Add optional fog-of-war hooks for future use.

### Design constraints
- Feedback systems should consume state, not own it.
- Fog support should be hooks only unless needed sooner.

---

## Step 13 - Save/load-ready match state and replay-friendly commands

### Objective
Prepare the architecture for persistence and replay by making important state and commands serializable.

### Playable result
The game remains playable, while match snapshots or command logs become possible for debugging and future features.

### Todo
- Define serializable match snapshot schema.
- Define command serialization format.
- Ensure content references are ID-based.
- Add deterministic seed handling for spawns and random behavior.
- Add save/load hooks or replay prototype.

### Design constraints
- Serialization should be an infrastructure concern.
- Determinism must be protected where practical.

---

## Step 14 - Multiplayer foundation with lockstep-friendly commands

### Objective
Introduce multiplayer groundwork in a way that does not require rewriting domain systems.

### Playable result
At minimum, a prototype multiplayer-ready architecture exists, and basic local simulation can consume player command streams from distinct participants.

### Todo
- Define player input command packets.
- Separate local player control from player identity in simulation.
- Add network service abstraction.
- Add match tick synchronization model.
- Prototype local host plus client or simulated dual-player input.
- Validate deterministic outcomes from identical command streams.

### Design constraints
- Avoid sending raw state every frame as the primary model.
- Favor command replication if deterministic simulation is stable enough.
- Networking code must not invade domain logic.

---

## Step 15 - AI, content pipeline hardening, and mod support polish

### Objective
Finish the foundation by improving extensibility, validation, tooling, and mod workflows.

### Playable result
The game is a strong vertical slice and can grow via content packs, mods, additional towers, new maps, new factions, and later more advanced multiplayer.

### Todo
- Add mod discovery and layered content loading.
- Add schema validation and startup content report.
- Add sample mod overriding stats or adding towers.
- Add developer console or debug commands.
- Add basic AI hooks for future co-op or versus modes.
- Add content error reporting with file path and entity ID.
- Add balancing utilities and developer cheats.

### Design constraints
- Mods should be able to add or override content with predictable precedence.
- Invalid content must fail clearly, not silently.

## Testing Strategy Across All Steps

From the first gameplay systems onward, add tests in layers:

- Unit tests for domain logic
- Integration tests for content loading and validation
- Determinism tests for movement, combat timing, and wave spawning where practical
- Command tests for build, sell, move, and upgrade actions
- Serialization tests once save or network packets exist

Prioritize tests for:

- Map build validation
- Path traversal
- Wave spawning schedule
- Economy changes
- Tower targeting
- Damage and death
- Upgrade eligibility
- Command execution rules
- Content loading failures

## Performance Guidance From the Start

- Use fixed timestep simulation.
- Keep allocation low in hot update loops.
- Keep immutable definitions separate from runtime instances.
- Avoid scanning the whole world when spatial partitioning becomes necessary.
- Use IDs and registries for definitions.
- Introduce culling, batching, and spatial structures only when profiling says they matter.
- Keep debug instrumentation available from early stages.

## AI-Agent Guidance Summary

Each task should be small enough that an AI agent can complete it safely without architectural drift.

For every step, the agent should:

- Implement only the scope of that step.
- Preserve public interfaces and previously working behavior.
- Avoid rewriting stable code unless strictly necessary.
- Extend through new classes, new config entries, and new tests.
- Keep content data externalized whenever the new feature introduces configurable behavior.
- Update README and Project Structure documentation.

## Definition of Done Per Step

A step is done when:

- The game launches and remains playable.
- Tests pass.
- New behavior is documented.
- No previous playable behavior is broken.
- New systems are placed in the correct layer.
- New configurable gameplay values are externalized.
- The code remains clean and reasonably simple.
