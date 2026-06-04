# Interactic

[![curseforge](https://img.shields.io/badge/-CurseForge-gray?style=for-the-badge&logo=curseforge&labelColor=orange)](https://www.curseforge.com/minecraft/mc-mods/interactic)
[![modrinth](https://img.shields.io/badge/-modrinth-gray?style=for-the-badge&labelColor=green&labelWidth=15&logo=appveyor&logoColor=white)](https://modrinth.com/mod/interactic)
[![discord](https://img.shields.io/discord/825828008644313089?label=wisp%20forest&logo=discord&logoColor=white&style=for-the-badge)](https://discord.gg/xrwHKktV2d)

## 概述

Interactic 为掉落物添加了更多的交互方式。它的许多功能灵感来源于 ItemPhysic，但本模组完全基于 Fabric 构建。

## 功能

所有功能均可在配置中单独开关。

- **花式物品渲染**：物品掉落时会根据下落速度以不同速度旋转。落地后平躺在地面上，堆叠物品会紧凑排列。
- **增强拾取与提示**：可以右键拾取物品，即使距离几格远。准星对准掉落物时，准星下方会显示物品提示。
- **物品过滤器**：物品过滤器允许你精细控制自动拾取的物品类型。可配置为白名单或黑名单模式，被列表影响的物品需要手动右键拾取或潜行拾取。也可以在配置中全局禁用自动拾取。
- **物品投掷**：按住丢弃键一段时间可以投掷物品。按住越久，投掷越远。带有伤害属性修饰符的物品（如剑、斧）在投掷命中目标时会造成伤害。
- **仅客户端模式**：可在仅客户端模式下使用 Interactic，自动禁用所有服务端功能，仅保留增强渲染和提示。

## 需求

- Minecraft **1.21.11**
- Fabric Loader **>=0.16.0**
- Fabric API
- [owo-lib](https://modrinth.com/mod/owo-lib) **>=0.13.0**

## 配置

通过 ModMenu 进行配置。所有选项可在 Interactic 分类下找到。在仅客户端模式下，服务端选项会从配置界面中隐藏。

## 从 1.21 升级到 1.21.11 的修改说明

此版本基于社区贡献从原 MC 1.21 版本移植到 1.21.11（2025年12月的 Mounts of Mayhem 更新）。

### 构建工具链变更

| 组件 | 旧版本 (1.21) | 新版本 (1.21.11) |
|-----------|-------------|----------------|
| Gradle | 8.8 | 9.4.0 |
| Fabric Loom | 1.7-SNAPSHOT | 1.15.5 |
| Fabric Loader | 0.15.11 | 0.19.3 |
| Fabric API | 0.100.1+1.21 | 0.141.4+1.21.11 |
| owo-lib | 0.12.10+1.21 | 0.13.0+1.21.11 |
| ModMenu | 11.0.0-rc.2 | 17.0.0 |
| Mappings | Yarn (1.21+build.2) | **Mojang Official** |
| Java | 21 | 21 |

### Minecraft API 主要变更

- **Mojang 官方映射**：所有类名、方法名、字段名从 Fabric Yarn 改为 Mojang 官方名称。例如 `PlayerEntity` → `Player`，`Identifier` 从 `net.minecraft.util` 移至 `net.minecraft.resources`
- **实体渲染重构 (1.21.5+)**：旧的 `EntityRenderer.render()` 方法被替换为 `extractRenderState()` + `submit()`。`ItemEntityRendererMixin` 完全重写以适配新的延迟渲染管线（`SubmitNodeCollector`），使用 `ItemEntityRenderState` 存储每个实体的渲染数据
- **物品注册变更**：`Item.Properties` 现在需要在构造 `Item` 之前调用 `.setId(ResourceKey)`。`ItemFilterItem` 接受预配置的 `Properties` 参数
- **物品栏结构变更**：Yarn 的 `PlayerInventory.combinedInventory` 在 Mojang 中被替换为单个 `items` `NonNullList<ItemStack>`。Accessor mixin 已相应更新
- **Player.drop() 变更**：基类 `Player` 的 `drop` 方法从 3 参数 `(ItemStack, boolean, boolean)` 改为 2 参数 `(ItemStack, boolean)`。`ServerPlayer` 保留了 3 参数重载
- **合成配方系统**：`RecipeManager.apply()` 现在使用 `RecipeMap` 而非 `Map<Identifier, JsonElement>`。已移除配方注入 mixin，改为数据包 JSON 提供物品过滤器配方
- **GUI 渲染**：`GuiGraphics.blit()` 签名变更——UV 坐标从像素坐标改为归一化坐标 (0-1)
- **按键绑定 API**：`KeyMapping` 构造函数现在接受 `KeyMapping.Category` 而非 `String`
- **物品模型谓词**：`ModelPredicateProviderRegistry` (Yarn) / `ItemProperties` (Mojang) 在 1.21.2+ 中被基于 JSON 的模型选择系统替代。Java 注册代码已注释，等待迁移到新模型格式
- **Mixin 注入点适配**：所有 mixin 注入点的方法名和描述符都已从 Yarn 更新为 Mojang 名称，并适配了方法签名变化

### 修复的问题

- 修复了物品过滤器配方在 1.21.2+ 新原料格式下的解析错误
- 修复了 `Player.drop()` 方法参数数量变化导致的投掷力度不生效问题
- 修复了右键拾取利用 mixin `@At("HEAD")` 注入 `startUseItem`，可在空手和持有物品时正常工作
- 修复了拾取动画——现在通过设置 `pickupDelay` 然后调用 `playerTouch()` 来触发原版拾取动效
- 修复了跨实体渲染数据混淆——通过在 `ItemEntityRenderState` 上存储每实体数据解决
- 修复了方块物品的渲染深度检测，改进平放/非平放判断

### 已知限制

- **物品模型覆层（启用/禁用贴图）**：代码中的模型谓词注册尚未迁移到 1.21.2+ 基于 JSON 的 `SelectItemModel` 系统。物品过滤器无论状态始终显示相同贴图
- **配方条件**：物品过滤器配方始终加载，不受 `itemFilterEnabled` 配置选项控制（物品被禁用时配方仍然存在但无法合成）

## 协议

MIT
