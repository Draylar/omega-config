package draylar.omegaconfig.api.screen.widget.supplier;

import draylar.omegaconfig.api.screen.widget.WidgetSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;

public class StringWidgetSupplier implements WidgetSupplier<String, AbstractButtonWidget> {

    @Override
    public AbstractButtonWidget create(int x, int y, int width, int height, LiteralText prompt, String value) {
        return new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x, y, width, height, new LiteralText(value));
    }

    @Override
    public String get(AbstractButtonWidget widget) {
        return widget.getMessage().asString();
    }
}
