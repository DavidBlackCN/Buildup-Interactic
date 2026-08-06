# Buildup Interactic

[![curseforge](https://img.shields.io/badge/-CurseForge-gray?style=for-the-badge&logo=curseforge&labelColor=orange)](https://www.curseforge.com/minecraft/mc-mods/interactic)
[![modrinth](https://img.shields.io/badge/-modrinth-gray?style=for-the-badge&labelColor=green&labelWidth=15&logo=appveyor&logoColor=white)](https://modrinth.com/mod/interactic)
[![discord](https://img.shields.io/discord/825828008644313089?label=wisp%20forest&logo=discord&logoColor=white&style=for-the-badge)](https://discord.gg/xrwHKktV2d)

## Overview

Buildup Interactic adds more ways to interact with dropped items. It is a port of [Interactic](https://github.com/gliscowo/interactic) to Minecraft 26.1.2, with further polish on pickup, throwing, and rendering interactions on top of the original.

## Features

All of these features can be individually toggled in the config.

- **Fancy Item Rendering**: Items will spin while they fall, at changing speed depending on how fast they're falling. When on the ground, they'll lay flat and compact stacks.
- **Enhanced Pickup and Tooltips**: You can pick up items by right-clicking them, even from a few blocks away. When looking at one, you'll see its tooltip rendered below your crosshair.
- **Item Filter**: A per-player filter that finely controls which items you automatically pick up — no item to craft or carry. Open the filter screen from the mod config or a (by default unbound) keybind, and choose whitelist or blacklist mode (blacklist by default, so you pick up everything until you say otherwise). While holding an item, press the quick-add key (`i` by default) to toggle it in or out of the filter list, with a message confirming the change. Items blocked by the filter can still be grabbed deliberately by right-clicking them, or — if enabled in the config — by sneaking over them.
- **Item Throwing**: You can throw items by holding down the drop key for a period of time. The longer you hold it, the further you'll throw. Items which have a damage modifier on them, like Swords and Axes, will also deal damage when they hit something after being thrown this way.
- **Client-Only Mode**: You can use Interactic in a mode where it will automatically disable all server-side features and give you only the enhanced rendering and tooltips.

## Requirements

- Minecraft **26.1.2**
- Java **25 or newer**
- Fabric Loader **>=0.19.3**
- Fabric API
- [YetAnotherConfigLib (YACL)](https://modrinth.com/mod/yacl) **>=3.9.6**

## Configuration

Interactic can be configured via ModMenu. All options can be found under the Interactic category. If you're running in client-only mode, server-side options will be hidden from the config screen. The config screen also has an "Open Item Filter" button to open the filter screen directly while in game.

## License

MIT
