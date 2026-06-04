# Interactic

[![curseforge](https://img.shields.io/badge/-CurseForge-gray?style=for-the-badge&logo=curseforge&labelColor=orange)](https://www.curseforge.com/minecraft/mc-mods/interactic)
[![modrinth](https://img.shields.io/badge/-modrinth-gray?style=for-the-badge&labelColor=green&labelWidth=15&logo=appveyor&logoColor=white)](https://modrinth.com/mod/interactic)
[![discord](https://img.shields.io/discord/825828008644313089?label=wisp%20forest&logo=discord&logoColor=white&style=for-the-badge)](https://discord.gg/xrwHKktV2d)

## Overview

Interactic adds more ways to interact with dropped items, hence the name. Many of its features are inspired by ItemPhysic, this mod has however been built from the ground up for Fabric.

## Features

All of these features can be individually toggled in the config.

- **Fancy Item Rendering**: Items will spin while they fall, at changing speed depending on how fast they're falling. When on the ground, they'll lay flat and compact stacks.
- **Enhanced Pickup and Tooltips**: You can pick up items by right-clicking them, even from a few blocks away. When looking at one, you'll see its tooltip rendered below your crosshair.
- **Item Filter**: The Item Filter is an item that allows you to finely control which items you want to automatically pick up. You can configure it to use either a whitelist or blacklist and any items affected by that list will require you to explicitly pick them up using either a right-click or by sneaking. You can also globally disable auto-pickup in the config to force everyone to explicitly pick up all items.
- **Item Throwing**: You can throw items by holding down the drop key for a period of time. The longer you hold it, the further you'll throw. Items which have a damage modifier on them, like Swords and Axes, will also deal damage when they hit something after being thrown this way.
- **Client-Only Mode**: You can use Interactic in a mode where it will automatically disable all server-side features and give you only the enhanced rendering and tooltips.

## Requirements

- Minecraft **1.21.11**
- Fabric Loader **>=0.16.0**
- Fabric API
- [owo-lib](https://modrinth.com/mod/owo-lib) **>=0.13.0**

## Configuration

Interactic can be configured via ModMenu. All options can be found under the Interactic category. If you're running in client-only mode, server-side options will be hidden from the config screen.

## Upgrade Notes (1.21 → 1.21.11)

This version is a major update from the original MC 1.21 release, ported by the community. See [README_zh.md](README_zh.md) for the Chinese version.

### Build Toolchain Changes

| Component | Old (1.21) | New (1.21.11) |
|-----------|-----------|---------------|
| Gradle | 8.8 | 9.4.0 |
| Fabric Loom | 1.7-SNAPSHOT | 1.15.5 |
| Fabric Loader | 0.15.11 | 0.19.3 |
| Fabric API | 0.100.1+1.21 | 0.141.4+1.21.11 |
| owo-lib | 0.12.10+1.21 | 0.13.0+1.21.11 |
| ModMenu | 11.0.0-rc.2 | 17.0.0 |
| Mappings | Yarn (1.21+build.2) | **Mojang Official** |
| Java | 21 | 21 |

### Minecraft API Changes

- **Mojang Official Mappings**: All class names, method names, and field names switched from Fabric Yarn mappings to Mojang's official names (e.g., `PlayerEntity` → `Player`, `ItemStack` → `ItemStack`, `Identifier` → `Identifier` in `net.minecraft.resources`)
- **Entity Rendering Refactor (1.21.5+)**: The old `EntityRenderer.render()` method was replaced with `extractRenderState()` + `submit()`. `ItemEntityRendererMixin` was completely rewritten to use the new deferred rendering pipeline with `SubmitNodeCollector` and per-entity `ItemEntityRenderState` data.
- **Item Registration**: `Item.Properties` now requires `.setId(ResourceKey)` before the `Item` constructor is called. `ItemFilterItem` accepts pre-configured `Properties`.
- **Inventory Structure**: `PlayerInventory.combinedInventory` (Yarn) was replaced by a single `items` `NonNullList<ItemStack>` in Mojang. The accessor mixin was updated accordingly.
- **Player.drop()**: Method signature changed from 3 parameters `(ItemStack, boolean, boolean)` to 2 parameters `(ItemStack, boolean)` on the base `Player` class. `ServerPlayer` retains a 3-parameter overload.
- **Recipe System**: `RecipeManager.apply()` now uses `RecipeMap` instead of `Map<Identifier, JsonElement>`. The recipe injection mixin was removed; the Item Filter recipe is now provided as a data pack JSON.
- **GUI Rendering**: `GuiGraphics.blit()` signature changed — UV coordinates are now normalized (0-1) instead of pixel coordinates.
- **KeyMapping API**: `KeyMapping` constructor now takes a `KeyMapping.Category` instead of a `String` for the category parameter.
- **Item Model Predicates**: `ModelPredicateProviderRegistry` (Yarn) / `ItemProperties` (Mojang) was replaced by a JSON-based model selection system in 1.21.2+. The Java registration is commented out pending migration to the new model format.

### Known Limitations

- **Item model overrides (enabled/disabled texture)**: The in-code model predicate registration is not yet migrated to 1.21.2+'s JSON-based `SelectItemModel` system. The item filter item always shows the same texture regardless of state.
- **Recipe conditional**: The Item Filter recipe is always loaded regardless of the `itemFilterEnabled` config option (it simply won't craft if the item is disabled).

## License

MIT
