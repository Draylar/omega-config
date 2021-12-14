package draylar.omegaconfiggui.api.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.api.screen.widget.LabelWidget;
import draylar.omegaconfiggui.api.screen.widget.TypeWidgets;
import draylar.omegaconfiggui.api.screen.widget.WidgetSupplier;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Pair;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OmegaConfigScreen<T extends Config> extends Screen {

    private static final List<Class<?>> validClasses = Arrays.asList(Double.class, String.class, Boolean.class);
    private final T config;
    private final Screen parent;
    private final Map<Field, Pair<WidgetSupplier<Object, ClickableWidget>, ClickableWidget>> fieldWidgets = new HashMap<>();

    public OmegaConfigScreen(T config, Screen parent) {
        super(new LiteralText(""));
        this.config = config;
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();

        try {
            // add label & button for each config entry
            // TODO: SUB-CLASS SUPPORT
            List<Field> collect = Arrays.stream(config.getClass().getDeclaredFields()).filter(field -> validClasses.contains(TypeWidgets.unbox(field.getType()))).collect(Collectors.toList());
            int over = 0;

            for (Field field : collect) {
                Class<?> unbox = TypeWidgets.unbox(field.getType());

                // label
                addDrawable(new LabelWidget(150, 15 + height / 6 + 20 * over, new LiteralText(field.getName())));

                // get value
                field.setAccessible(true);
                Object value = field.get(config);

                // button / interact
                ClickableWidget button;
                WidgetSupplier<Object, ClickableWidget> widgetSupplier = (WidgetSupplier<Object, ClickableWidget>) TypeWidgets.get(unbox);
                if (widgetSupplier != null) {
                    button = widgetSupplier.create(this, 250, 5 + height / 6 + 25 * over, 100, 20, new LiteralText(field.getName()), value);
                } else {
                    button = new TextFieldWidget(client.textRenderer, 250, 5 + height / 6 + 25 * over, 100, 20, new LiteralText(field.getName()));
                }

                fieldWidgets.put(field, new Pair<>(widgetSupplier, button));

                addSelectableChild(button);

                over++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // save and exit
        ButtonWidget exit = new ButtonWidget(width - 60, height - 30, 50, 20, new LiteralText("Exit"), widget -> onClose());
        ButtonWidget save = new ButtonWidget(10, height - 30, 50, 20, new LiteralText("Save"), widget -> {
            fieldWidgets.forEach((field, pair) -> {
                try {
                    field.set(config, pair.getLeft().get(pair.getRight()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            config.save();
        });
        addSelectableChild(save);
        addSelectableChild(exit);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        //TODO make this work. for Drawables (children) it works in parent method
        /*
        for (ClickableWidget button : this.) {
            button.render(matrices, mouseX, mouseY, delta);
        }

        for (Element element : this.children) {
            if (element instanceof Drawable && !buttons.contains(element)) {
                ((Drawable) element).render(matrices, mouseX, mouseY, delta);
            }
        }

         */
    }

    @Override
    public void onClose() {
        client.setScreen(parent);
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        // TODO: option for transparent BG
//        this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
        renderCustomBackgroundTexture(0, 64, 64, 64, 0, 0, this.height, this.width);
        renderCustomBackgroundTexture(0, 32, 32, 32, 0, this.height / 8, this.height / 8 * 6, this.width);
        textRenderer.draw(matrices, config.getName(), width / 2f - textRenderer.getWidth(config.getName()) / 2f, 10, 0xffffff);
    }

    public void renderCustomBackgroundTexture(int vOffset, int r, int g, int b, int x, int y, int height, int width) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        this.client.getTextureManager().bindTexture(OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(x, height + y, 0.0D).texture(0.0F, (float) height / 32.0F + (float) vOffset).color(r, g, b, 255).next();
        bufferBuilder.vertex(width + x, height + y, 0.0D).texture((float) width / 32.0F, (float) height / 32.0F + (float) vOffset).color(r, g, b, 255).next();
        bufferBuilder.vertex(width + x, y, 0.0D).texture((float) width / 32.0F, (float) vOffset).color(r, g, b, 255).next();
        bufferBuilder.vertex(x, y, 0.0D).texture(0.0F, (float) vOffset).color(r, g, b, 255).next();
        tessellator.draw();
    }
}
