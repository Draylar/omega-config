package draylar.omegaconfiggui.api.screen.widget;

import draylar.omegaconfiggui.mixin.ClickableWidgetInvoker;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class BaseTextFieldWidget extends TextFieldWidget {

    protected final Screen parent;

    public BaseTextFieldWidget(Screen parent, TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        this.parent = parent;
    }

    @Override
    public void setTextFieldFocused(boolean focused) {
        // unfocus all others
        parent.children().forEach(element -> {
            if (element instanceof ClickableWidget) {
                ((ClickableWidgetInvoker) element).callSetFocused(false);
            }
        });

        super.setFocused(focused);
    }
}
