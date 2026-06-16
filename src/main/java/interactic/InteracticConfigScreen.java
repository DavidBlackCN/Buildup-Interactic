package interactic;

import interactic.util.InteracticNetworking;
import interactic.util.ServerSideConfigOption;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.OptionComponentFactory;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class InteracticConfigScreen extends ConfigScreen {

    protected InteracticConfigScreen(@Nullable Screen parent) {
        super(DEFAULT_MODEL_ID, InteracticInit.getConfig(), parent);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        super.build(rootComponent);

        // Only useful in-game, where there is a player to open the filter for
        if (Minecraft.getInstance().player == null) return;

        var optionPanel = rootComponent.childById(FlowLayout.class, "option-panel");
        if (optionPanel == null) return;

        var button = UIComponents.button(Component.translatable("text.interactic.open_filter"), b -> {
            InteracticNetworking.CHANNEL.clientHandle().send(new InteracticNetworking.OpenFilterScreen());
            this.onClose();
        }).horizontalSizing(Sizing.fill(100));
        button.margins(Insets.vertical(4));

        optionPanel.child(0, button);
    }

    @Override
    protected @Nullable OptionComponentFactory<?> factoryForOption(Option<?> option) {
        return InteracticInit.getConfig().clientOnlyMode() && option.backingField().hasAnnotation(ServerSideConfigOption.class)
                ? null
                : super.factoryForOption(option);
    }
}
