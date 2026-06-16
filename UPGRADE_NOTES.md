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

### Fixes & Polish

- Fixed the item filter recipe failing to parse under the 1.21.2+ ingredient format.
- Fixed throwing power not applying due to the `Player.drop()` parameter change.
- Right-click pickup is injected at `@At("HEAD")` of `startUseItem`, so it works both empty-handed and while holding an item.
- The pickup animation is restored by setting `pickupDelay` and calling `playerTouch()` to trigger the vanilla pickup effect.
- Fixed cross-entity render data bleeding by storing per-entity data on `ItemEntityRenderState`.
- Improved block-item render depth detection for the flat/non-flat decision.
- Fixed the item name not showing under the crosshair: `GuiGraphics.drawString` now requires an alpha channel in 1.21.11, so the color was changed from `0xFFFFFF` to `0xFFFFFFFF`.
- Restored the ground-settle animation style: the settle step is scaled by the partial tick again (instead of a fixed value), so items rotate flat smoothly rather than snapping instantly.
- Relaxed the right-click pickup hit test to match the original, so items can be grabbed mid-air while in flight.
- Polished pickup so that grabbing a block empty-handed or while holding a block no longer accidentally places it.


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
- 修复了准星指向掉落物时不显示物品名的问题：`GuiGraphics.drawString` 在 1.21.11 要求颜色带 alpha 通道，颜色值由 `0xFFFFFF` 改为 `0xFFFFFFFF`
- 还原了掉落物落地归位动画的样式：归位步长由固定值改回与原版一致的按 partial tick 缩放，使物品平滑转正而非瞬间贴地
- 放宽了右键拾取的命中判定，使其与原版一致，可在物品飞行途中右键拾取
- 优化了空手拾取方块/手持方块拾取物品时不会误放置方块的行为


## 0.2.0 — Item Filter Rework

The Item Filter was reworked from a craftable physical item into a per-player setting. See the section below for the Chinese version.

### Changes

- **Removed the Item Filter item**: the `ItemFilterItem`, its data components, model/texture, and crafting recipe are gone. The filter is now stored per-player via the Fabric Data Attachment API (`ItemFilter`), persists across death, and no longer occupies an inventory slot.
- **Opening the filter screen**: open it from the mod config screen's "Open Item Filter" button, or from a keybind (unbound by default). The screen (slots + blacklist/allowlist buttons) reuses the original layout.
- **Quick add/remove**: while holding an item, press the quick-add key (`i` by default) to toggle that item in or out of the filter list, with a confirmation message.
- **Blacklist by default**: a player picks up everything until items are added to a blacklist (or allowlist mode is selected).
- **Right-click overrides the filter**: deliberately right-clicking an item picks it up even if the filter would block it (a transient force-pickup flag bypasses the filter check, above the filter logic). Sneaking to ignore the filter is now a config toggle (`filterSneakOverride`, on by default).
- **Targeting consistency**: strict hit-testing applies only to grounded items, so aiming near a dropped item no longer hijacks right-click block placement; airborne items keep the loose targeting so they can be caught mid-flight. The crosshair tooltip uses the same rule.

### 物品过滤重做（0.2.0）

物品过滤从可合成的实体物品重做为玩家级别的配置。

- **移除了物品过滤器物品**：`ItemFilterItem`、其数据组件、模型/贴图与合成配方都已移除。过滤配置现在通过 Fabric 数据附件 API（`ItemFilter`）按玩家存储，死亡后保留，不再占用物品栏格子。
- **打开过滤页面**：可在 mod 配置界面的"打开物品过滤"按钮打开，或使用快捷键（默认不绑定）。页面（槽位 + 黑/白名单按钮）沿用原有布局。
- **快速添加/移除**：手持物品时按快捷键（默认 `i`）即可将该物品加入或移出过滤列表，并显示提示信息。
- **默认黑名单**：默认情况下玩家拾取所有物品，直到将物品加入黑名单（或切换为白名单模式）。
- **右键拾取凌驾于过滤之上**：主动右键拾取的物品即使被过滤挡住也能捡起（通过一个瞬态的强制拾取标记绕过过滤判定，位于过滤逻辑之上）。潜行忽略过滤改为配置开关（`filterSneakOverride`，默认开启）。
- **命中判定一致性**：严格命中只对地上的物品生效，因此对准地上掉落物附近不会再抢占右键放置方块；空中物品保持宽松命中，可在飞行途中拾取。准星下的物品名提示也遵循同一规则。