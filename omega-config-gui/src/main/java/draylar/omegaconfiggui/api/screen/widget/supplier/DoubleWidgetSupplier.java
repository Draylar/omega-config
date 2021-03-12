package draylar.omegaconfiggui.api.screen.widget.supplier;

import draylar.omegaconfiggui.api.screen.widget.WidgetSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;

public class DoubleWidgetSupplier implements WidgetSupplier<Double, AbstractButtonWidget> {

    @Override
    public AbstractButtonWidget create(int x, int y, int width, int height, LiteralText prompt, Double value) {
        return new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x, y, width, height, new LiteralText(String.valueOf(value)));
    }

    @Override
    public Double get(AbstractButtonWidget widget) {
        return Double.parseDouble(widget.getMessage().asString());
    }
}
