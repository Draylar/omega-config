package draylar.omegaconfiggui.api.screen.widget.supplier;

import draylar.omegaconfiggui.api.screen.widget.DoubleFieldWidget;
import draylar.omegaconfiggui.api.screen.widget.WidgetSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class DoubleWidgetSupplier implements WidgetSupplier<Double, DoubleFieldWidget> {

    @Override
    public DoubleFieldWidget create(Screen parent, int x, int y, int width, int height, LiteralText prompt, Double value) {
        LiteralText text = new LiteralText(String.valueOf(value));
        DoubleFieldWidget widget = new DoubleFieldWidget(parent, MinecraftClient.getInstance().textRenderer, x, y, width, height, text);
        widget.setText(text.asString());
        return widget;
    }

    @Override
    public Double get(DoubleFieldWidget widget) {
        String message = widget.getText();

        // if it is just a . or empty, return 0
        if(message.equals(".") || message.isEmpty()) {
            return 0.0;
        }

        // trim potential trailing .
        if(message.indexOf(".") == message.length()) {
            message = message.substring(0, message.length() - 1);
        }

        return Double.parseDouble(message);
    }
}
