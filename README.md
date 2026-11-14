# Reload Everything

A client-side Fabric mod that adds a ⟳ button to three screens, so you can reload things with one click.

| Where | What it does |
| --- | --- |
| Title screen | Restarts the game process: launches a new instance with the current process's real argv (including -Xmx and other JVM arguments), then exits the old one |
| Pause menu (singleplayer only) | Saves the current world and loads it again, waiting for the save to finish before reloading |
| Resource pack screen | Reloads resource packs and returns you to the screen you came from |

Every action asks for confirmation first.

## Requirements

- Minecraft 26.3
- Fabric Loader 0.19.3
- Fabric API 0.161.0+26.3
- Java 25

## Building

Windows: `gradlew.bat build` — other platforms: `./gradlew build`

The jar is written to `build/libs/`.

## Notes

- Process-level restart is skipped in a development environment (Fabric dev run); only a soft reload is performed.

## License

MIT
