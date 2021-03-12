package draylar.omegaconfig.api.screen.widget;

import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.text.LiteralText;

public interface WidgetSupplier<T, A extends AbstractButtonWidget> {
    AbstractButtonWidget create(int x, int y, int width, int height, LiteralText prompt, T value);
    T get(A widget);
}
