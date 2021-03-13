package draylar.omegaconfiggui.api.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class DoubleFieldWidget extends BaseTextFieldWidget {

    public DoubleFieldWidget(Screen parent, TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(parent, textRenderer, x, y, width, height, text);
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
