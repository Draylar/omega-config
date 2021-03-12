package draylar.omegaconfig.api.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class LabelWidget extends DrawableHelper implements Drawable, Element {

    private final int x;
    private final int y;
    private final Text message;

    public LabelWidget(int x, int y, Text message) {
        this.x = x;
        this.y = y;
        this.message = message;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        drawTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, message, x, y, 0xffffff);
    }
}
