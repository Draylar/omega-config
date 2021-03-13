package draylar.omegaconfiggui.api.screen.widget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.LiteralText;

public interface WidgetSupplier<T, A extends AbstractButtonWidget> {
    AbstractButtonWidget create(Screen parent, int x, int y, int width, int height, LiteralText prompt, T value);
    T get(A widget);
}
