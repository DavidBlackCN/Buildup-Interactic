package interactic;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import interactic.util.InteracticNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class InteracticConfigScreen {

    private InteracticConfigScreen() {}

    public static Screen create(Screen parent) {
        var config = InteracticInit.getConfig();
        boolean serverOptionsAvailable = !config.clientOnlyMode();

        var global = ConfigCategory.createBuilder()
                .name(Component.translatable("text.config.interactic.section.global"))
                .option(booleanOption("clientOnlyMode", false, config::clientOnlyMode, config::clientOnlyMode, true, true))
                .option(booleanOption("rightClickPickup", true, config::rightClickPickup, config::rightClickPickup, true, serverOptionsAvailable))
                .option(booleanOption("itemThrowing", true, config::itemThrowing, config::itemThrowing, true, serverOptionsAvailable))
                .option(booleanOption("itemFilterEnabled", true, config::itemFilterEnabled, config::itemFilterEnabled, false, serverOptionsAvailable))
                .option(booleanOption("filterSneakOverride", true, config::filterSneakOverride, config::filterSneakOverride, false, serverOptionsAvailable))
                .option(booleanOption("itemsActAsProjectiles", true, config::itemsActAsProjectiles, config::itemsActAsProjectiles, false, serverOptionsAvailable))
                .option(booleanOption("autoPickup", true, config::autoPickup, config::autoPickup, false, serverOptionsAvailable));

        if (Minecraft.getInstance().player != null && config.itemFilterEnabled()) {
            global.option(ButtonOption.createBuilder()
                    .name(Component.translatable("text.interactic.open_filter"))
                    .text(Component.translatable("text.interactic.open_filter"))
                    .action(screen -> {
                        InteracticNetworking.sendOpenFilterScreen();
                        screen.onClose();
                    })
                    .build());
        }

        var client = ConfigCategory.createBuilder()
                .name(Component.translatable("text.config.interactic.section.client"))
                .option(booleanOption("fancyItemRendering", true, config::fancyItemRendering, config::fancyItemRendering, false, true))
                .option(booleanOption("renderItemTooltips", true, config::renderItemTooltips, config::renderItemTooltips, false, true))
                .option(booleanOption("renderFullTooltip", false, config::renderFullTooltip, config::renderFullTooltip, false, true))
                .option(booleanOption("swingArm", true, config::swingArm, config::swingArm, false, true))
                .option(booleanOption("blocksLayFlat", false, config::blocksLayFlat, config::blocksLayFlat, false, true))
                .build();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("text.config.interactic.title"))
                .category(global.build())
                .category(client)
                .save(config::save)
                .build()
                .generateScreen(parent);
    }

    private static Option<Boolean> booleanOption(String key, boolean defaultValue, Supplier<Boolean> getter,
                                                  Consumer<Boolean> setter, boolean restartRequired, boolean available) {
        var builder = Option.<Boolean>createBuilder(Boolean.class)
                .name(Component.translatable("text.config.interactic.option." + key))
                .description(OptionDescription.of(Component.translatable("text.config.interactic.option." + key + ".description")))
                .binding(defaultValue, getter, setter)
                .controller(BooleanControllerBuilder::create)
                .available(available);

        if (restartRequired) builder.flag(OptionFlag.GAME_RESTART);
        return builder.build();
    }
}
