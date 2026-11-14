# Reload Everything

A client-side Fabric mod that adds a ⟳ button to three screens, so you can reload things with one click.

| Where | What it does |
| --- | --- |
| Title screen | Restarts the game process: launches a new instance with the current process's real argv (including -Xmx and other JVM arguments), then exits the old one |
| Pause menu (singleplayer only) | Saves the current world and loads it again, waiting for the save to finish before reloading |
| Resource pack screen | Reloads resource packs and returns you to the screen you came from |

The settings screen is opened from [Mod Menu](https://modrinth.com/mod/modmenu): every button can be toggled, the confirmation prompt can be turned off, and restart mode can be switched between process restart and soft reload.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.158.0+26.2
- Cloth Config API 26.2.155
- Mod Menu 20.0.3 (optional, needed to open the settings screen)
- Java 25

## Building

Windows: `gradlew.bat build` — other platforms: `./gradlew build`

The jar is written to `build/libs/`.

## Notes

- Process-level restart is skipped in a development environment (Fabric dev run); only a soft reload is performed.

## Source

https://github.com/EX-ist/reload-everything

## License

MIT
