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
| **Ignore public chat** | Public chat written by an ignored player is dropped before it reaches your chat window. |
| **Ignore private messages** | Whispers from an ignored player are dropped. A separate switch, so you can silence someone in public chat and still let them reach you privately, or the other way round. |
| **Ignore rendering** | Ignored players are drawn transparently, or hidden completely at transparency 0. Armour, held items, name tags and shadows all follow the same setting. |
| **Ignore tab list** | Ignored players are removed from the player list you see when holding Tab. |
| **Ignore server name plates** | Hides floating name plates that show an ignored player's name, for servers that draw their own instead of using the vanilla name tag. Off by default. |
| **Ignore particles** | Drops particles landing within three blocks of an ignored player, for plugins that give players particle effects. Off by default, and a proximity guess rather than ownership. |
| **Mute in Simple Voice Chat** | Sets ignored players to 0% in [Simple Voice Chat](https://modrinth.com/mod/simple-voice-chat) and back to 100% once they leave the list. Off by default, and does nothing without that mod installed. |
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
- [Simple Voice Chat](https://modrinth.com/mod/simple-voice-chat) — optional, needed only for the muting option

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
| `'` | Toggle ignore public chat |
| unbound | Toggle ignore tab list |
| unbound | Toggle click through ignored players |

The two punctuation keys are bound by physical position, not by the character printed on
them, so their label depends on your keyboard layout. On a German layout they are `Ö` and
`Ä`. The controls screen always shows the key you actually have to press.

### Commands

These are client commands: they run on your client only and nothing is sent to the
server. Running `/ignoring` on its own prints this list in game.

| Command | What it does |
| --- | --- |
| `/ignoring addignore <player>` | Add a player to the ignore list. Tab completes from everyone online who is not on it yet |
| `/ignoring removeignore <player>` | Remove a player from the ignore list. Tab completes from the list |
| `/ignoring listignore` | Show everyone currently ignored |
| `/ignoring togglerender` | Toggle ignore rendering |
| `/ignoring togglechat` | Toggle hiding public chat from ignored players |
| `/ignoring togglemsg` | Toggle hiding whispers from ignored players |
| `/ignoring toggletablist` | Toggle ignore tab list |
| `/ignoring togglenameplates` | Toggle hiding server drawn name plates |
| `/ignoring toggleparticles` | Toggle hiding particles around ignored players |
| `/ignoring togglevoicechat` | Toggle muting ignored players in Simple Voice Chat |
| `/ignoring toggleinteraction` | Toggle click through ignored players |
| `/ignoring transparency <0-255>` | Set how visible ignored players stay |
| `/ignoring reload` | Reload the config file from disk |
| `/ignoring debug chat` | Write incoming chat to the log file, to work out how a server formats its messages |
| `/ignoring version` | Show which build of Ignoring is installed |
| `/ignoring help` | List every command |

### Public chat and private messages

These are two independent switches. **Ignore Public Chat** drops what an ignored player
writes in the channel everyone reads; **Ignore Private Messages** drops their whispers.
Turning on one does not touch the other, so you can silence someone in public and still let
them reach you privately, or block their whispers while still following the main chat. Both
are off by default.

Telling the two apart is not something the client can do on its own. Most servers format
chat themselves and send the finished line as plain text, which throws away the marker
Minecraft would normally use. What survives is the clickable name: servers make it start a
whisper, and that click carries the player's real name. On a public line only the name is
clickable, on a whisper the whole `[A » B]` header is — and that difference is what the mod
reads. It needs no knowledge of the server's language or punctuation, and it uses the real
account name rather than a nickname the server displays.

On a server that does not make names clickable, no such marker exists. Everything then
counts as public chat, filtered by matching the player's name against the text, and whispers
cannot be told apart. `/ignoring debug chat` writes incoming chat to `logs/latest.log` so you
can see which of the two your server does.

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

## Translations

The mod follows whatever language Minecraft is set to. English and German ship with it,
and Korean is there from the original author.

Adding one is a single file and needs no code. Copy
`src/main/resources/assets/ignoring/lang/en_us.json` to the language code you want, for
example `fr_fr.json`, and translate the values. Leave the keys alone, keep the `%s` and
`%d` placeholders in place, and save the file as UTF-8. Minecraft falls back to English
for any key a file does not have, so a partial translation works fine and can be filled
in later.

| File | Language |
| --- | --- |
| `en_us.json` | English |
| `de_de.json` | German |
| `ko_kr.json` | Korean |

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
