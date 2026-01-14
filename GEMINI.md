# Minestom-MiniGames Project Context

## Project Overview
This project is a modular Minecraft mini-game server built on the **Minestom** framework. It focuses on high-performance, custom gameplay mechanics.

A **`TODO.MD`** file is maintained at the root to track planned improvements, bug fixes, and future features.

### Key Technologies
- **Framework:** Minestom (1.21.4+)
- **Language:** Java 21+
- **Build System:** Gradle (Kotlin DSL)
- **Data:** JSON (Map configuration), Anvil (World format)

## Architecture

The server uses a centralized Manager system to handle game lifecycles and mechanics.

### Core Managers
- **`GameManager`**: Handles the lifecycle of games (Lobby -> Game -> End -> Hub). Manages players joining and quitting.
- **`HubManager`**: Manages multiple Hub instances. Handles player connection, state reset (inventory, gamemode, health), and load balancing between hubs.
- **`MapManager`**: Loads world instances from the `maps/` directory in **read-only** mode (Anvil format). Parses `config.json` for team spawns and game rules.
- **`StructureManager`**: Saves and loads structures (NBT format) from/to the `structures/` directory. Compatible with Minecraft Structure Blocks.
- **`DamageManager`**: Centralizes damage handling (`DamageSources`). Standardizes damage types (Mob Attack, PvP, Magic, Explosion) and knockback logic.
- **`PvpManager`**: Manages PvP specific events (invulnerability frames, attack cooldowns) via `DamageManager`.
- **`DeathManager`**: Handles player death without the native red screen. Puts players in a **Ghost Mode** (Adventure, Invisible, Flight) and manages respawn timers or elimination. Includes **Titles** (YOU DIED), **Sounds**, and automatic **Void Protection**.
- **`ProjectileManager`**: Handles custom projectiles (e.g., Sheep projectiles, explosive arrows). Uses native physics for movement and custom collision logic.
- **`PotionManager`**: Handles vanilla-like potion effects (Regeneration, Poison, Instant Health/Damage, etc.).

### Game Structure
All mini-games extend the abstract `Game` class.
- **`DuelGame`**: 1v1 PvP arena. Elimination mode (no respawn).
- **`SheepWarsGame`**: Team-based tactical game. Players launch sheep with special abilities to destroy the enemy team.

## Key Gameplay Mechanics

### Game Modes & State
- **Hub/Lobby**: Players are forced into **Adventure Mode** (no block breaking). Empty lobbies are automatically destroyed to save resources.
- **Game (SheepWars)**: Players are set to **Survival Mode** (can break blocks, take damage).
- **Dismount**: Players can exit vehicles/mounts (like the Boarding Sheep) by **sneaking**, if allowed by the game rules (`allowDismountSneak`).

### Structure System
Allows saving and placing NBT structures (Schematics).
...
### Scoreboard System
Each game features a dynamic **Sidebar** (Scoreboard) that displays:
- The game name and current state (Lobby, In-Game, Ending).
- A real-time **Kill Counter** ranking the top 5 players.

### SheepWars System
A complex projectile system with 17 unique sheep types.
- **Registry**: `SheepRegistry` maps custom items (White Wool + CustomModelData) to Sheep factories.
- **Physics**: Sheep are launched as `EntityCreature` with applied velocity (native physics).
- **Activation**: Generalized **activation delay** (`activationDelay`) handled by the base class.
- **Spawning Rules**: Problematic sheeps (Party, Clone, Glutton) are blacklisted from being spawned by the **Party Sheep**.
- **Abilities**:
    - **Explosive**: Explodes after 3s.
    - **Boarding**: Carries the shooter.
    - **Taupe**: Digs tunnels while flying.
    - **Black Hole**: Attracts entities.
    - **Heal, Ice, Fire, Lightning, Earthquake...** (17 total).
- **Utils**: `TKit` (ported from Bukkit) provides spatial queries (`getBlocksInSphere`, `getEntitiesInRadius`).

### Map System
Maps are stored in `maps/<map_name>/`.
- **Hybrid Loading**: 
    - If `region/` exists: Loads world data via `AnvilLoader`.
    - If `region/` is missing: Generates a **Void world** and pastes structures defined in `config.json`.
- `config.json`: Configuration for spawns, teams, optional structures, and **void threshold** (`voidY`).

```json
{
  "name": "Arena",
  "minPlayers": 2,
  "maxPlayers": 8,
  "voidY": -10.0,
  "structures": [
    { "name": "island", "pos": {"x": 0, "y": 64, "z": 0}, "rotation": "0", "mirror": "none" },
    { "name": "island", "pos": {"x": 50, "y": 64, "z": 0}, "rotation": "180", "mirror": "x" }
  ],
  "teams": [
    { "name": "Red", "color": "#FF0000", "spawns": [...] },
    { "name": "Blue", "color": "#0000FF", "spawns": [...] }
  ]
}
```

## Building and Running

### Prerequisites
- Java 21 JDK (Java 25 recommended for latest Minestom features)

### Commands
- **Build**: `./gradlew build` (Use `--no-daemon` if IO errors occur in WSL).
- **Run**: `./gradlew run`

### In-Game Commands
- `/play <DUEL|SHEEP_WARS>`: Join a game queue.
- `/givewool <ID>`: Give a specific special sheep (e.g., `/givewool explosive`).
- `/structure save <x1> <y1> <z1> <x2> <y2> <z2> <name>`: Save a structure.
- `/structure load <x> <y> <z> <name> [0/90/180/270] [none/x/z/xz]`: Load a structure with optional rotation and mirror.
- `/debug`: Give basic equipment.
- `/instances`: Debug command to list active instances and player counts.

## Development Conventions
- **Workflow Rules**:
    - **Feature Addition**: When adding a new feature, always attempt to compile (`./gradlew build -x test`), resolve any issues (using documentation tools if necessary), commit the changes, and finally update `GEMINI.md` to document the new functionality.
    - **Agreement First**: During the discussion or debriefing of an idea, no code modifications should be made until the user has provided general agreement on the proposed plan.
- **Events**: Use `EventNode` for scoping events to specific games or instances.
- **Entities**: Use `EntityCreature` for custom mobs. Avoid `AbstractProjectile` for simple physics, prefer native velocity.
- **Items**: Use `ItemBuilder` (in `me.holypite.utils`) to handle Minestom 1.21.4+ `CustomModelData` (complex component format).
- **Thread Safety**: Maps and Lists in Managers should be thread-safe (`ConcurrentHashMap`, `CopyOnWriteArrayList`) where appropriate.

## Available Documentation Tools

### Minestom Documentation Tools
Used to explore the Minestom framework source and documentation.
- `list_packages`: List all available Java packages in Minestom.
- `list_classes_in_package`: List classes and interfaces in a specific package.
- `search_class`: Search for a class by name.
- `search_code_content`: Search for keywords inside Minestom code.
- `read_class_code`: Read the source code of a Minestom class.
- `read_readme`: Read the Minestom project README.

### Minecraft Documentation Tools
Used to explore Minecraft-related source files and packets.
- `list_folders`: List sub-folders in a given path.
- `list_files_in_folder`: List Java files in a specific folder.
- `search_code_scoped`: Search for keywords limited to a specific folder.
- `read_minecraft_class`: Read the source code of a Minecraft-related class.
- `find_packet`: Search specifically for Packet files.
