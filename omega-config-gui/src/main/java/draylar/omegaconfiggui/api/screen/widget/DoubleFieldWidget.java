package draylar.omegaconfiggui.api.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class DoubleFieldWidget extends TextFieldWidget {

    public DoubleFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public void write(String string) {
        try {
            if(string.equals(".") && !getText().contains(".")) {
                super.write(string);
            } else {
                Double.parseDouble(string);
                super.write(string);
            }

        } catch(NumberFormatException ignored) {

        }
    }
}
