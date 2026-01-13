# Minestom-MiniGames Project Context

## Project Overview
This project is a modular Minecraft mini-game server built on the **Minestom** framework (implementing Minecraft 1.21.4 server protocol). It focuses on high-performance, custom gameplay mechanics without the overhead of standard Bukkit/Spigot servers.

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
- **`PvpManager`**: Custom PvP implementation. Handles damage calculation and **custom vector-based knockback** (replacing Minestom's default).
- **`DeathManager`**: Handles player death without the native red screen. Puts players in a **Ghost Mode** (Adventure, Invisible, Flight) and manages respawn timers or elimination.
- **`ProjectileManager`**: Handles custom projectiles (e.g., Sheep projectiles, explosive arrows). Uses native physics for movement and custom collision logic.

### Game Structure
All mini-games extend the abstract `Game` class.
- **`DuelGame`**: 1v1 PvP arena. Elimination mode (no respawn).
- **`SheepWarsGame`**: Team-based tactical game. Players launch sheep with special abilities to destroy the enemy team.

## Key Gameplay Mechanics

### SheepWars System
A complex projectile system with 17 unique sheep types.
- **Registry**: `SheepRegistry` maps custom items (White Wool + CustomModelData) to Sheep factories.
- **Physics**: Sheep are launched as `EntityCreature` with applied velocity (native physics).
- **Abilities**:
    - **Explosive**: Explodes after 3s.
    - **Boarding**: Carries the shooter.
    - **Taupe**: Digs tunnels while flying.
    - **Black Hole**: Attracts entities.
    - **Heal, Ice, Fire, Lightning, Earthquake...** (17 total).
- **Utils**: `TKit` (ported from Bukkit) provides spatial queries (`getBlocksInSphere`, `getEntitiesInRadius`).

### Map System
Maps are stored in `maps/<map_name>/`.
- `region/*.mca` + `level.dat`: World data.
- `config.json`: Configuration for spawns and teams.
```json
{
  "name": "Arena",
  "minPlayers": 2,
  "maxPlayers": 8,
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
- `/debug`: Give basic equipment.
- `/instances`: Debug command to list active instances and player counts.

## Development Conventions
- **Events**: Use `EventNode` for scoping events to specific games or instances.
- **Entities**: Use `EntityCreature` for custom mobs. Avoid `AbstractProjectile` for simple physics, prefer native velocity.
- **Items**: Use `ItemBuilder` (in `me.holypite.utils`) to handle Minestom 1.21.4+ `CustomModelData` (complex component format).
- **Thread Safety**: Maps and Lists in Managers should be thread-safe (`ConcurrentHashMap`, `CopyOnWriteArrayList`) where appropriate.
