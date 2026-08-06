# Buildup Interactic

[![curseforge](https://img.shields.io/badge/-CurseForge-gray?style=for-the-badge&logo=curseforge&labelColor=orange)](https://www.curseforge.com/minecraft/mc-mods/interactic)
[![modrinth](https://img.shields.io/badge/-modrinth-gray?style=for-the-badge&labelColor=green&labelWidth=15&logo=appveyor&logoColor=white)](https://modrinth.com/mod/interactic)
[![discord](https://img.shields.io/discord/825828008644313089?label=wisp%20forest&logo=discord&logoColor=white&style=for-the-badge)](https://discord.gg/xrwHKktV2d)

## 概述

Buildup Interactic 为掉落物添加了更多的交互方式。它基于 [Interactic](https://github.com/gliscowo/interactic) 移植至 Minecraft 26.1.2，并在原版基础上对拾取、投掷与渲染等交互细节做了进一步打磨。

## 功能

所有功能均可在配置中单独开关。

- **花式物品渲染**：物品掉落时会根据下落速度以不同速度旋转。落地后平躺在地面上，堆叠物品会紧凑排列。
- **增强拾取与提示**：可以右键拾取物品，即使距离几格远。准星对准掉落物时，准星下方会显示物品提示。
- **物品过滤**：物品过滤让你精细控制自动拾取的物品类型。每个玩家拥有一份独立的过滤配置（不再是一个实体物品），可配置为白名单或黑名单模式（默认黑名单，即默认拾取所有物品）。可用快捷键或在 mod 配置界面中打开过滤页面（快捷键默认不绑定）；也可手持物品按快捷键（默认 `i`）将该物品快速加入/移出过滤列表，并显示提示信息。当物品因过滤而无法拾取时，潜行即可忽略过滤强行拾取（此特性可在配置中开关，默认开启）。
- **物品投掷**：按住丢弃键一段时间可以投掷物品。按住越久，投掷越远。带有伤害属性修饰符的物品（如剑、斧）在投掷命中目标时会造成伤害。
- **仅客户端模式**：可在仅客户端模式下使用 Interactic，自动禁用所有服务端功能，仅保留增强渲染和提示。

## 需求

- Minecraft **26.1.2**
- Java **25 或更高版本**
- Fabric Loader **>=0.19.3**
- Fabric API
- [YetAnotherConfigLib (YACL)](https://modrinth.com/mod/yacl) **>=3.9.6**

## 配置

通过 ModMenu 进行配置。所有选项可在 Interactic 分类下找到。在仅客户端模式下，服务端选项会从配置界面中隐藏。配置界面中还有一个“打开物品过滤”按钮，可在游戏内直接打开过滤页面。

## 协议

MIT
