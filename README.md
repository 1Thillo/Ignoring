# Ignoring

A client-side Fabric mod that lets you mute and hide individual players. Put a name on
the ignore list and Ignoring can stop their chat messages from reaching you, fade or
hide their character in the world, drop them from the tab list, and let you click
straight through them.

Everything happens on your own client. The server is never told anything, no other
player is affected, and nothing you do here changes what anyone else sees.

[Downloads on Modrinth](https://modrinth.com/mod/ignoring/versions)

## What it can do

| Feature | What it does |
| --- | --- |
| **Ignore chat** | Chat messages that mention an ignored player are dropped before they reach your chat window. |
| **Ignore rendering** | Ignored players are drawn transparently, or hidden completely at transparency 0. Armour, held items, name tags and shadows all follow the same setting. |
| **Ignore tab list** | Ignored players are removed from the player list you see when holding Tab. |
| **Ignore server name plates** | Hides floating name plates that show an ignored player's name, for servers that draw their own instead of using the vanilla name tag. Off by default. |
| **Ignore particles** | Drops particles landing within three blocks of an ignored player, for plugins that give players particle effects. Off by default, and a proximity guess rather than ownership. |
| **Click through** | Blocks and entities behind an ignored player stay clickable, so someone standing in your way cannot block you. |
| **Ignore everyone** | Applies all of the above to every player except yourself, without listing them one by one. |
| **Strip trailing symbols** | Removes a single decorative symbol from the end of incoming chat messages. Applies to all messages, not only ignored players. |
| **Transparency** | How visible ignored players stay, from 0 (invisible) to 255 (fully solid). This starts at 255, which means turning on *ignore rendering* alone changes nothing you can see until you lower it. |

## Requirements

- Minecraft 26.2
- Java 25 or newer
- [Fabric Loader](https://fabricmc.net/use/) 0.19.5 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [Mod Menu](https://modrinth.com/mod/modmenu) — optional, adds a settings button next to the mod in the mod list

## Using it

### Settings screen

Press **P**, or open the settings from Mod Menu. Every option has a tooltip explaining
what it does.

### Hotkeys

All of these can be rebound under Options → Controls → Ignoring Hotkeys.

| Default key | Action |
| --- | --- |
| `P` | Open the settings screen |
| `;` | Toggle ignore rendering |
| `'` | Toggle ignore chat |
| unbound | Toggle ignore tab list |
| unbound | Toggle click through ignored players |

### Commands

These are client commands: they run on your client only and nothing is sent to the
server. Running `/ignoring` on its own prints this list in game.

| Command | What it does |
| --- | --- |
| `/ignoring addignore <player>` | Add a player to the ignore list. Tab completes from everyone online who is not on it yet |
| `/ignoring removeignore <player>` | Remove a player from the ignore list. Tab completes from the list |
| `/ignoring listignore` | Show everyone currently ignored |
| `/ignoring togglerender` | Toggle ignore rendering |
| `/ignoring togglechat` | Toggle ignore chat |
| `/ignoring toggletablist` | Toggle ignore tab list |
| `/ignoring togglenameplates` | Toggle hiding server drawn name plates |
| `/ignoring toggleparticles` | Toggle hiding particles around ignored players |
| `/ignoring toggleinteraction` | Toggle click through ignored players |
| `/ignoring transparency <0-255>` | Set how visible ignored players stay |
| `/ignoring reload` | Reload the config file from disk |
| `/ignoring version` | Show which build of Ignoring is installed |
| `/ignoring help` | List every command |

## Known issues

**Rows of the ignore list can end up outside the clickable area of the settings
screen.** The ignore list is the last entry on the screen, and every row you add makes
it taller. Once a row sits below the visible part of the list, clicks no longer reach
its text field, so it looks like an unresponsive input box. Scrolling the list so the
row sits well inside the visible area makes it editable again; a lower GUI scale or a
larger window gives it more room.

If you would rather not fight the screen, `/ignoring addignore <player>` and
`/ignoring removeignore <player>` edit the same list and always work. You can also edit
`config/ignoring.json` by hand and run `/ignoring reload`.

## Versions

This repository keeps one branch per Minecraft version. Pick the branch that matches
the version you play; `26.2` is the current one.

## Building from source

Java 25 is required.

```bash
./gradlew build
```

The finished jar lands in `build/libs/`.

## Credits

Written by [stellium1](https://github.com/stellium1).

| Minecraft version | Ported by |
| --- | --- |
| 1.21.4 | [relpia](https://github.com/relpia) |
| 1.21.8 | [VectorTransformation](https://github.com/VectorTransformation) |
| 26.2 | [Thillo](https://github.com/1Thillo) |

The transparent rendering is derived from
[Transparent-Entities](https://github.com/LopyMine/Transparent-Entities) by
[LopyMine](https://github.com/LopyMine), used under the LGPL-3.0.

## License

GNU Lesser General Public License v3.0. See [LICENSE](LICENSE).

![Screenshot of the settings screen](https://github.com/user-attachments/assets/06a5ff17-5676-44b1-a304-17b25a10e166) ![Screenshot of an ignored player rendered transparently](https://github.com/user-attachments/assets/dcdc4012-a3c6-4c65-916e-25d1ba66f1af)
