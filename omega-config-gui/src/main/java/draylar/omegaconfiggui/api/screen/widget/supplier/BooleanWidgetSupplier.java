package draylar.omegaconfiggui.api.screen.widget.supplier;

import draylar.omegaconfiggui.api.screen.widget.WidgetSupplier;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.LiteralText;

public class BooleanWidgetSupplier implements WidgetSupplier<Boolean, CheckboxWidget> {

    @Override
    public CheckboxWidget create(Screen parent, int x, int y, int width, int height, LiteralText prompt, Boolean value) {
        return new CheckboxWidget(x, y, 20, 20, new LiteralText(""), value);
    }

    @Override
    public Boolean get(CheckboxWidget widget) {
        return widget.isChecked();
    }
}
