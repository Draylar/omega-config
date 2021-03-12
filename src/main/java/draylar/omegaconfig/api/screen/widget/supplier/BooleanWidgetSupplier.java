package draylar.omegaconfig.api.screen.widget.supplier;

import draylar.omegaconfig.api.screen.widget.WidgetSupplier;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.LiteralText;

public class BooleanWidgetSupplier implements WidgetSupplier<Boolean, CheckboxWidget> {

    @Override
    public CheckboxWidget create(int x, int y, int width, int height, LiteralText prompt, Boolean value) {
        return new CheckboxWidget(x, y, 20, 20, new LiteralText(""), value);
    }

    @Override
    public Boolean get(CheckboxWidget widget) {
        return widget.isChecked();
    }
}
