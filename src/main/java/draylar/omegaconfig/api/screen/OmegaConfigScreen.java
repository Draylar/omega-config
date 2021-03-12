package draylar.omegaconfig.api.screen;

import draylar.omegaconfig.api.Config;
import draylar.omegaconfig.api.screen.widget.LabelWidget;
import draylar.omegaconfig.api.screen.widget.TypeWidgets;
import draylar.omegaconfig.api.screen.widget.WidgetSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class OmegaConfigScreen<T extends Config> extends Screen {

    private static final List<Class<?>> validClasses = Arrays.asList(Double.class, String.class, Boolean.class);
    private final T config;
    private final Screen parent;
    private final Map<Field, Pair<WidgetSupplier<Object, AbstractButtonWidget>, AbstractButtonWidget>> fieldWidgets = new HashMap<>();

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
                addChild(new LabelWidget(150, 5 + 20 * over, new LiteralText(field.getName())));

                // get value
                field.setAccessible(true);
                Object value = field.get(config);

                // button / interact
                AbstractButtonWidget button;
                WidgetSupplier<Object, AbstractButtonWidget> widgetSupplier = (WidgetSupplier<Object, AbstractButtonWidget>) TypeWidgets.get(unbox);
                if(widgetSupplier != null) {
                    button = widgetSupplier.create(250, 5 + 20 * over, 50, 50, new LiteralText(field.getName()), value);
                } else {
                    button = new TextFieldWidget(client.textRenderer, 250, 5 + 20 * over, 50, 20, new LiteralText(field.getName()));
                }

                fieldWidgets.put(field, new Pair<>(widgetSupplier, button));

                addButton(button);

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
        addButton(save);
        addButton(exit);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        for (AbstractButtonWidget button : this.buttons) {
            button.render(matrices, mouseX, mouseY, delta);
        }

        for (Element element : this.children) {
            if (element instanceof Drawable && !buttons.contains(element)) {
                ((Drawable) element).render(matrices, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public void onClose() {
        client.openScreen(parent);
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);

        MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("textures/block/dirt.png"));
        drawTexture(matrices, 0, 0, 0, 0, 16, 16);
    }
}
