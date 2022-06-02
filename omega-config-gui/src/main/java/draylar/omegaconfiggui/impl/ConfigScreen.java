package draylar.omegaconfiggui.impl;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import draylar.omegaconfiggui.api.screen.ScreenBuilder;
import draylar.omegaconfiggui.api.screen.ScreenEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

//Major parts of this class are based/taken from Cloth Config Lite by shedaniel, which is licensed under the MIT license.
/*
    Copyright 2021 shedaniel

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class ConfigScreen extends Screen {
    private static final int TOP = 26;
    private static final int BOTTOM = 24;
    public static final float SCROLLBAR_BOTTOM_COLOR = .5f;
    public static final float SCROLLBAR_TOP_COLOR = .67f;
    public double scrollerAmount;
    private boolean dragging;

    private final ScreenBuilder builder;
    private final Screen parent;
    private final List<BaseOption<?, ?>> options = new ArrayList<>();

    private static final Map<Class<?>, Function<Object, BaseOption<?, ?>>> SUPPORTED = ImmutableMap.<Class<?>, Function<Object, BaseOption<?, ?>>>builder()
            .put(Enum.class, v -> new ToggleOption<>((List<Enum<?>>) Arrays.asList(v.getClass().getEnumConstants()), ConfigScreen::toText))
            .put(Boolean.class, v -> new ToggleOption<>(Arrays.asList(Boolean.TRUE, Boolean.FALSE), ConfigScreen::toText))
            .put(String.class, v -> new TextFieldOption<>(Function.identity(), Function.identity()))
            .put(Integer.class, v -> new TextFieldOption<>(Objects::toString, Integer::valueOf))
            .put(Long.class, v -> new TextFieldOption<>(Objects::toString, Long::valueOf))
            .put(Double.class, v -> new TextFieldOption<>(Objects::toString, Double::valueOf))
            .put(Float.class, v -> new TextFieldOption<>(Objects::toString, Float::valueOf))
            .put(BigInteger.class, v -> new TextFieldOption<>(Objects::toString, BigInteger::new))
            .put(BigDecimal.class, v -> new TextFieldOption<>(Objects::toString, BigDecimal::new))
            //gson doesn't support it, and we don't have any type adapter for it, so commented it out
            //.put(Identifier.class, v -> new TextFieldOption<>(Objects::toString, Identifier::new))
            .build();

    public ConfigScreen(ScreenBuilder builder, Screen parent) {
        super(builder.title);
        this.builder = builder;
        this.parent = parent;
    }

    private static Text toText(Enum<?> e) {
        return Text.of(e.toString());
    }

    private static Text toText(Boolean b) {
        return Text.translatable("omegaconfig." + b.toString());
    }

    private <T> boolean add(Text text, T value, @Nullable Supplier<T> defaultValue, Consumer<T> savingConsumer, int y) {
        var func = SUPPORTED.get(value.getClass());

        if (func == null) {
            return false; //not supported, skip
        }
        var option = (BaseOption<T, ?>) func.apply(value);

        option.text = text;
        option.defaultValue = defaultValue;
        option.savingConsumer = savingConsumer;
        option.originalValue = value;
        option.value = value;
        option.y = y;
        options.add(option);
        option.onAdd();
        return true;
    }

    @Override
    protected void init(){
        int y = 30;
        int buttonWidths = Math.min(200, (width - 50 - 12) / 3);
        for (var entry : builder.entries) {
            if (entry instanceof ScreenEntry.FieldEntry f) {
                Field field = FieldUtils.getField(f.receiverClass(), f.name());
                Object value;
                try {
                    value = field.get(f.instance());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                var bool = add(f.getName(), value,
                    () -> {
                        try {
                            return field.get(f.getDefault());
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    val -> {
                        try {
                            field.set(f.instance(), val);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                        f.parent().save();
                    },
                    y
                );

                if (!bool) continue;
            } else if (entry instanceof ScreenEntry.SubScreenEntry s) {
                addDrawableChild(
                        new ButtonWidget(
                                width / 2 - buttonWidths - 3,
                                y + 1,
                                buttonWidths * 2,
                                20,
                                s.getName(),
                                b -> client.setScreen(s.screen().get())
                        )
                );
            }
            y += 20;
        }

        ((List<Element>) children()).addAll(options);
        addDrawableChild(new ClothConfigScreenButtons(this, width / 2 - buttonWidths - 3, height - 22, buttonWidths, 20, Text.empty(), true));
        addDrawableChild(new ClothConfigScreenButtons(this, width / 2 + 3, height - 22, buttonWidths, 20, Text.empty(), false));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        overlayBackground(matrices, TOP, height - BOTTOM, 32);
        for (BaseOption<?, ?> option : options) {
            option.render(client, textRenderer, 40, option.y, width - 80, 22, matrices, mouseX, mouseY, delta);
        }

        renderScrollBar();

        matrices.push();
        matrices.translate(0, 0, 500.0);
        overlayBackground(matrices, 0, TOP, 64);
        overlayBackground(matrices, height - BOTTOM, height, 64);
        renderShadow(matrices);
        drawCenteredText(matrices, textRenderer, getTitle(), width / 2, 9, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        matrices.pop();
    }

    private void renderScrollBar() {
        int listHeight = height - BOTTOM - TOP;
        int totalHeight = totalHeight();
        if (totalHeight > listHeight) {
            int maxScroll = Math.max(0, totalHeight - listHeight);
            int height = listHeight * listHeight / totalHeight;
            height = MathHelper.clamp(height, 32, listHeight);
            height = Math.max(10, height);
            int minY = Math.min(Math.max((int) scrollerAmount * (listHeight - height) / maxScroll + TOP, TOP), this.height - BOTTOM - height);

            int scrMaxX = width;
            int scrMinX = scrMaxX - 6;

            int maxY = this.height - BOTTOM;
            RenderSystem.disableTexture();
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            buffer.vertex(scrMinX, maxY, 0.0D).color(0, 0, 0, 255).next();
            buffer.vertex(scrMaxX, maxY, 0.0D).color(0, 0, 0, 255).next();
            buffer.vertex(scrMaxX, TOP, 0.0D).color(0, 0, 0, 255).next();
            buffer.vertex(scrMinX, TOP, 0.0D).color(0, 0, 0, 255).next();

            buffer.vertex(scrMinX, minY + height, 0.0D).color(SCROLLBAR_BOTTOM_COLOR, SCROLLBAR_BOTTOM_COLOR, SCROLLBAR_BOTTOM_COLOR, 1).next();
            buffer.vertex(scrMaxX, minY + height, 0.0D).color(SCROLLBAR_BOTTOM_COLOR, SCROLLBAR_BOTTOM_COLOR, SCROLLBAR_BOTTOM_COLOR, 1).next();
            buffer.vertex(scrMaxX, minY, 0.0D).color(SCROLLBAR_BOTTOM_COLOR, SCROLLBAR_BOTTOM_COLOR, SCROLLBAR_BOTTOM_COLOR, 1).next();
            buffer.vertex(scrMinX, minY, 0.0D).color(SCROLLBAR_BOTTOM_COLOR, SCROLLBAR_BOTTOM_COLOR, SCROLLBAR_BOTTOM_COLOR, 1).next();
            buffer.vertex(scrMinX, (minY + height - 1), 0.0D).color(SCROLLBAR_TOP_COLOR, SCROLLBAR_TOP_COLOR, SCROLLBAR_TOP_COLOR, 1).next();
            buffer.vertex((scrMaxX - 1), (minY + height - 1), 0.0D).color(SCROLLBAR_TOP_COLOR, SCROLLBAR_TOP_COLOR, SCROLLBAR_TOP_COLOR, 1).next();
            buffer.vertex((scrMaxX - 1), minY, 0.0D).color(SCROLLBAR_TOP_COLOR, SCROLLBAR_TOP_COLOR, SCROLLBAR_TOP_COLOR, 1).next();
            buffer.vertex(scrMinX, minY, 0.0D).color(SCROLLBAR_TOP_COLOR, SCROLLBAR_TOP_COLOR, SCROLLBAR_TOP_COLOR, 1).next();
            tess.draw();
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
        }
    }

    private void renderShadow(MatrixStack matrices) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 0, 1);
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, 0, TOP + 4, 0.0F).texture(0, 1).color(0, 0, 0, 0).next();
        buffer.vertex(matrix, width, TOP + 4, 0.0F).texture(1, 1).color(0, 0, 0, 0).next();
        buffer.vertex(matrix, width, TOP, 0.0F).texture(1, 0).color(0, 0, 0, 185).next();
        buffer.vertex(matrix, 0, TOP, 0.0F).texture(0, 0).color(0, 0, 0, 185).next();
        buffer.vertex(matrix, 0, height - BOTTOM, 0.0F).texture(0, 1).color(0, 0, 0, 185).next();
        buffer.vertex(matrix, width, height - BOTTOM, 0.0F).texture(1, 1).color(0, 0, 0, 185).next();
        buffer.vertex(matrix, width, height - BOTTOM - 4, 0.0F).texture(1, 0).color(0, 0, 0, 0).next();
        buffer.vertex(matrix, 0, height - BOTTOM - 4, 0.0F).texture(0, 0).color(0, 0, 0, 0).next();
        tess.draw();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected void overlayBackground(MatrixStack matrices, int minY, int maxY, int c) {
        var matrix = matrices.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        RenderSystem.setShaderTexture(0, OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, 0, maxY, 0.0F).texture(0 / 32.0F, maxY / 32.0F).color(c, c, c, 255).next();
        buffer.vertex(matrix, width, maxY, 0.0F).texture(width / 32.0F, maxY / 32.0F).color(c, c, c, 255).next();
        buffer.vertex(matrix, width, minY, 0.0F).texture(width / 32.0F, minY / 32.0F).color(c, c, c, 255).next();
        buffer.vertex(matrix, 0, minY, 0.0F).texture(0 / 32.0F, minY / 32.0F).color(c, c, c, 255).next();
        tess.draw();
    }

    public int scrollHeight() {
        int totalHeight = totalHeight();
        int listHeight = height - BOTTOM - TOP;
        if (totalHeight <= listHeight) {
            return 0;
        }
        return totalHeight - listHeight;
    }

    public int totalHeight() {
        int i = 8;
        for (BaseOption<?, ?> option : options) {
            i += 22;
        }
        return i;
    }

    public boolean hasErrors() {
        for (BaseOption<?, ?> option : options) {
            if (option.hasErrors) {
                return true;
            }
        }
        return false;
    }

    public boolean isEdited() {
        for (BaseOption<?, ?> option : options) {
            if (option.isEdited()) {
                return true;
            }
        }
        return false;
    }

    public void save() {
        for (BaseOption option : options) {
            option.save();
            option.originalValue = option.value;
        }
    }

    @Override
    public void close() {
        if (isEdited()) {
            client.setScreen(new ConfirmScreen(this::acceptConfirm, Text.translatable("omegaconfig.quit_config"),
                    Text.translatable("omegaconfig.quit_config_sure"),
                    Text.translatable("omegaconfig.quit_discard"),
                    Text.translatable("gui.cancel")));
        } else {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (e >= TOP && e <= height - BOTTOM) {
            scrollerAmount = MathHelper.clamp(scrollerAmount - f * 16.0D, 0, scrollHeight());
            return true;
        }
        return super.mouseScrolled(d, e, f);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        this.dragging = i == 0 && d >= width - 6 && d < width;
        return super.mouseClicked(d, e, i) || dragging;
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (super.mouseDragged(d, e, i, f, g)) {
            return true;
        }
        if (i != 0 || !this.dragging) {
            return false;
        }
        if (e < TOP) {
            scrollerAmount = 0;
        } else if (e > height - BOTTOM) {
            scrollerAmount = scrollHeight();
        } else {
            double h = Math.max(1, this.scrollHeight());
            int j = height - BOTTOM - TOP;
            int k = MathHelper.clamp((int) ((j * j) / (float) this.scrollHeight()), 32, j - 8);
            double l = Math.max(1.0, h / (j - k));
            scrollerAmount = MathHelper.clamp(scrollerAmount + g * l, 0, scrollHeight());
        }
        return true;
    }

    private void acceptConfirm(boolean t) {
        if (!t) {
            client.setScreen(this);
        } else {
            client.setScreen(parent);
        }
    }

    static class EditBox extends TextFieldWidget {
        public EditBox(TextRenderer font, int i, int j, int k, int l, Text text) {
            super(font, i, j, k, l, text);
        }

        @Override
        public void setTextFieldFocused(boolean f) {
            for (Element child : MinecraftClient.getInstance().currentScreen.children()) {
                if (child instanceof TextFieldOption<?> option) {
                    EditBox box = option.widget;
                    box.setFocused(box == this);
                }
            }
            super.setTextFieldFocused(f);
        }
    }

    static class ClothConfigScreenButtons extends PressableWidget {
        final ConfigScreen screen;
        final boolean cancel;

        public ClothConfigScreenButtons(ConfigScreen screen, int i, int j, int k, int l, Text text, boolean cancel) {
            super(i, j, k, l, text);
            this.screen = screen;
            this.cancel = cancel;
        }



        @Override
        public void render(MatrixStack poseStack, int i, int j, float f) {
            if (cancel) {
                setMessage(Text.translatable(screen.isEdited() ? "omegaconfig.cancel_discard" : "gui.cancel"));
            } else {
                boolean hasErrors = screen.hasErrors();
                active = screen.isEdited() && !hasErrors;
                setMessage(Text.translatable(hasErrors ? "omegaconfig.error" : "omegaconfig.save"));
            }
            super.render(poseStack, i, j, f);
        }

        @Override
        public void onPress() {
            if (cancel) {
                screen.close();
            } else {
                screen.save();
            }
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder nr) {
            nr.put(NarrationPart.USAGE, getMessage());
        }
    }

    static class TextFieldOption<T> extends BaseOption<T, EditBox> {
        private final Function<T, String> toString;
        private final Function<String, T> fromString;

        public TextFieldOption(Function<T, String> toString, Function<String, T> fromString) {
            this.toString = toString;
            this.fromString = fromString;
            this.widget = addChild(new EditBox(MinecraftClient.getInstance().textRenderer, 0, 0, 98, 18, null));
        }

        @Override
        public void onAdd() {
            widget.setMaxLength(1000000);
            widget.setText(toString.apply(value));
            widget.setChangedListener(this::update);
        }

        @Override
        public void render(MinecraftClient minecraft, TextRenderer font, int x, int y, int width, int height, MatrixStack matrices, int mouseX, int mouseY, float delta) {
            widget.setEditableColor(hasErrors ? 16733525 : 14737632);
            super.render(minecraft, font, x, y, width, height, matrices, mouseX, mouseY, delta);
        }

        private void update(String s) {
            try {
                this.value = fromString.apply(s);
                this.hasErrors = false;
            } catch (Exception e) {
                this.hasErrors = true;
            }
        }
    }

    static class ToggleOption<T> extends BaseOption<T, ButtonWidget> {
        private final List<T> options;
        private final Function<T, Text> toText;

        public ToggleOption(List<T> options, Function<T, Text> toText) {
            this.options = options;
            this.toText = toText;
            this.widget = addChild(new ButtonWidget(0, 0, 100, 20, Text.empty(), this::switchNext));
        }

        @Override
        public void onAdd() {
            widget.setMessage(toText.apply(value));
        }

        private void switchNext(ButtonWidget button) {
            value = options.get((options.indexOf(value) + 1) % options.size());
            onAdd();
        }
    }

    abstract static class BaseOption<T, W extends ClickableWidget> extends AbstractParentElement {
        public Text text;
        @Nullable
        public Supplier<T> defaultValue;
        public Consumer<T> savingConsumer;
        public T originalValue;
        public T value;
        public boolean hasErrors;
        public final List<? extends Element> children = new ArrayList<>();

        private final ButtonWidget resetButton = addChild(new ButtonWidget(0, 0, 46, 20, Text.of("Reset"), this::onResetPressed));

        public W widget;
        int y;

        private void onResetPressed(ButtonWidget button) {
            value = defaultValue.get();
            reset();
        }

        public void render(MinecraftClient minecraft, TextRenderer font, int x, int y, int width, int height, MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MutableText text = Text.of(this.text.getString()).copy();
            boolean edited = isEdited() || hasErrors;
            if (edited) {
                text.formatted(Formatting.ITALIC);
                if (hasErrors) {
                    text.styled(style -> style.withColor(16733525));
                }
            } else {
                text.formatted(Formatting.GRAY);
            }
            font.draw(matrices, text, x, y + 8, 0xFFFFFF);
            resetButton.x = x + width - 46;
            resetButton.y = y + 1;
            resetButton.active = isNotDefault();
            resetButton.render(matrices, mouseX, mouseY, delta);

            int i = (widget instanceof TextFieldWidget ? 1 : 0);
            widget.x = x + width - 100 - 48 + i;
            widget.y = y + i + 1;
            widget.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        protected <R extends Element> R addChild(R listener) {
            ((List) children).add(listener);
            return listener;
        }

        public void onAdd() {
        }

        protected void reset() {
            onAdd();
        }

        public boolean isEdited() {
            return !Objects.equals(originalValue, value);
        }

        protected boolean isNotDefault() {
            return defaultValue != null && !Objects.equals(defaultValue.get(), value);
        }

        public void save() {
            savingConsumer.accept(value);
        }
    }
}
