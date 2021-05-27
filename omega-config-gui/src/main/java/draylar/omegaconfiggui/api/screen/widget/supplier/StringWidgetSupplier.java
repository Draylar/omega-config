package draylar.omegaconfiggui.api.screen.widget.supplier;

import draylar.omegaconfiggui.api.screen.widget.BaseTextFieldWidget;
import draylar.omegaconfiggui.api.screen.widget.WidgetSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;

public class StringWidgetSupplier implements WidgetSupplier<String, TextFieldWidget> {

    @Override
    public TextFieldWidget create(Screen parent, int x, int y, int width, int height, LiteralText prompt, String value) {
        LiteralText text = new LiteralText(value);
        TextFieldWidget textField = new BaseTextFieldWidget(parent, MinecraftClient.getInstance().textRenderer, x, y, width, height, text);
        textField.setText(value);
        return textField;
    }

    @Override
    public String get(TextFieldWidget widget) {
        return widget.getText();
    }
}
