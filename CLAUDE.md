# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Compile
powershell -Command "cd 'D:\IntelliJprojects\The_Infernal_Depths-master(1)\The_Infernal_Depths-master'; .\gradlew.bat compileJava"

# Run all tests
powershell -Command "cd 'D:\IntelliJprojects\The_Infernal_Depths-master(1)\The_Infernal_Depths-master'; .\gradlew.bat test"

# Run a single test class
powershell -Command "cd 'D:\IntelliJprojects\The_Infernal_Depths-master(1)\The_Infernal_Depths-master'; .\gradlew.bat test --tests 'com.mikm.entities.prefabLoader.YAMLLoaderTest'"

# Run the game (desktop)
powershell -Command "cd 'D:\IntelliJprojects\The_Infernal_Depths-master(1)\The_Infernal_Depths-master'; .\gradlew.bat desktop:run"
```

The `del` command doesn't work in this environment. Use `rm` instead.

## Project Overview

The Infernal Depths is a pixel art cave exploration game built with **libGDX** (1.11.0) and **Ashley ECS** (1.7.4). It's a multi-module Gradle project (Java 8) with modules: `core` (game logic), `desktop` (launcher), `tests`.

Entry point: `DesktopLauncher` → `Application.getInstance()` (singleton). The game starts on `TownScreen`.

## Architecture

### ECS (Ashley Entity Component System)

All game logic follows ECS. Components are data-only classes in `_components/`, systems contain logic in `_systems/`.

**Key components:** `Transform` (position/velocity), `SpriteComponent` (rendering), `CombatComponent` (health/damage), `WorldColliderComponent` (collision), `RoutineListComponent` (state machine), `ComboStateComponent` (combo tracking), `LockOnComponent` (targeting), `AttackInputComponent` (input buffering).

**Key systems:** `RoutineSystem` (state machines), `WorldCollisionMovementSystem` (movement), `RenderingSystem` (draw ordering), `ComboSystem` (combo tree traversal), `LockOnSystem` (auto/manual targeting), `AttackInputSystem` (charge detection), `ProjectileSpawnSystem`, `AttackMovementSystem`.

### YAML Prefab System

Entities and weapons are defined in YAML files under `assets/yaml/`. The loading pipeline:

1. **Schema YAML** (e.g., `weapon.yaml`) provides defaults
2. **Instance YAML** (e.g., `copperSword.yaml`) overrides specific fields
3. `YAMLLoader` merges them into raw POJOs, then applies `FieldTransformer`s via `TransformerRegistry` to produce typed formatted data
4. `PrefabInstantiator` creates Ashley entities from the formatted data

Fields annotated `@Copyable` are deep-copied per entity instance. Fields annotated `@CopyReference` share the same object.

### Weapon & Combo System

Weapons define an **orbit** (how they visually orbit the player), a **combo tree** (branching attack sequences), and per-attack **config** data.

Combo trees branch on attack duration (LIGHT/MEDIUM/HEAVY based on charge time) and distance conditions (ANY/GREATER/LESS relative to locked enemy). Each `AttackNode` specifies an attack name, condition, and `thenNext`/`elseNext` branches.

### Action System

Actions define entity behavior states (extend `Action`). They integrate with the `RoutineListComponent` state machine. Lifecycle: `enter()` → `update()` → `onExit()`.

**Design rule:** Actions should be as encompassing as possible. Parameterize actions rather than creating new classes for similar behaviors. For example, `OrbitPlayerAction` with parameters handles both weapon orbiting and projectile orbiting — don't create separate classes like `SliceAction` and `WeaponAction` when the behavior is fundamentally the same.

### Routine State Machine

Entities have `Routine` objects containing cycles (lists of actions) and transitions (condition-based state changes). Transitions use a compiled condition system referencing entity state via a `Blackboard` singleton.

## Delta Time

Use `Gdx.graphics.deltaTime` for delta time, or `DeltaTime.deltaTimeMultiplier` for fps * delta time, essentially a scaler.

## Key Libraries

- **SnakeYAML** (2.3): YAML config parsing
- **Kryo** (5.4.0): Save file serialization (`assets/InfernalDepthsSaveFiles/`)
- **Reflections** (0.10.2): Runtime reflection for component/transformer discovery
- **ShapeDrawer** (2.5.0): Debug shape rendering

## Post-Task Hook

After completing any task, run:
```bash
curl -H "Title: Claude" -d "Claude task completed" https://ntfy.sh/claude-done-123
```
