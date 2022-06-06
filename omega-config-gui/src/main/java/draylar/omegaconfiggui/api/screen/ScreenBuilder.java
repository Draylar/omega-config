package draylar.omegaconfiggui.api.screen;

import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.impl.ConfigScreen;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A builder class to create {@link ConfigScreen}s
 */
public final class ScreenBuilder {
    private final Config parentConfig;
    public final List<ScreenEntry> entries = new ArrayList<>();
    public final Text title;
    public final Identifier backgroundTexture;
    @Nullable
    private final ScreenBuilder parent;
    private final Screen modMenuScreen;

    ScreenBuilder(Config parentConfig, Text title, @Nullable Identifier backgroundTexture, @Nullable ScreenBuilder parent, Screen modMenuScreen) {
        this.parentConfig = parentConfig;
        this.title = title;
        this.backgroundTexture = backgroundTexture;
        this.parent = parent;
        this.modMenuScreen = modMenuScreen;
    }

    /**
     * @param parentConfig the receiver config
     * @param title the screen title
     * @param backgroundTexture the screen's background texture. Use null for Minecraft's default.
     * @param modMenuScreen the parent screen
     * @return A new ScreenBuilder without a parent
     */
    public static ScreenBuilder create(Config parentConfig, Text title, @Nullable Identifier backgroundTexture, Screen modMenuScreen) {
        return new ScreenBuilder(parentConfig, title, backgroundTexture, null, modMenuScreen);
    }

    /**
     * @param parentConfig the receiver config
     * @param title the screen title
     * @param modMenuScreen the parent screen
     * @return A new ScreenBuilder without a parent
     */
    public static ScreenBuilder create(Config parentConfig, Text title, Screen modMenuScreen) {
        return create(parentConfig, title, null, modMenuScreen);
    }

    /**
     * Use this when creating sub-screens for a config screen.
     * @param parentConfig the receiver config
     * @param title the screen title
     * @param backgroundTexture the screen's background texture. Use null for Minecraft's default.
     * @param parent this builder's parent
     * @return A new builder with a parent
     */
    public static ScreenBuilder createSubScreen(Config parentConfig, Text title, @Nullable Identifier backgroundTexture, ScreenBuilder parent) {
        return new ScreenBuilder(parentConfig, title, backgroundTexture, parent, parent.modMenuScreen);
    }

    /**
     * Use this when creating sub-screens for a config screen.
     * @param parentConfig the receiver config
     * @param title the screen title
     * @param parent this builder's parent
     * @return A new builder with a parent
     */
    public static ScreenBuilder createSubScreen(Config parentConfig, Text title, ScreenBuilder parent) {
        return createSubScreen(parentConfig, title, null, parent);
    }


    /**
     * @param entries {@link ScreenEntry}s to add to the resulting screen
     * @return self
     */
    public ScreenBuilder addEntries(ScreenEntry... entries) {
        Collections.addAll(this.entries, entries);
        return this;
    }

    /**
     * @param entries {@link ScreenEntry}s to add to the resulting screen
     * @return self
     */
    public ScreenBuilder addEntries(Collection<ScreenEntry> entries) {
        this.entries.addAll(entries);
        return this;
    }

    /**
     * @return the resulting screen for this builder
     */
    public Screen build() {
        return new ConfigScreen(this, this.parent == null ? modMenuScreen : this.parent.build());
    }

    public ScreenBuilder run(UnaryOperator<ScreenBuilder> b) {
        return b.apply(this);
    }


    /**
     * Creates a new builder that has {@code this} as a parent.
     * @param title the resulting screen title
     * @param backgroundTexture the screen's background texture. Use null for Minecraft's default.
     * @return a new builder from self
     */
    public ScreenBuilder newBuilderWithSelfParent(Text title, Identifier backgroundTexture) {
        var sub = createSubScreen(this.parentConfig, title, backgroundTexture, this);
        addEntries(subScreenEntry(sub::build, title));

        return sub;
    }

    /**
     * Creates a new builder that has {@code this} as a parent.
     * @param title the resulting screen title
     * @return a new builder from self
     */
    public ScreenBuilder newBuilderWithSelfParent(Text title) {
        return newBuilderWithSelfParent(title, null);
    }

    /**
     * @return the parent of this builder
     */
    @Nullable
    public ScreenBuilder getParent() {
        return parent;
    }

    /**
     * @return If non-null, returns this screen's background texture. Otherwise, returns Minecraft's default background texture.
     */
    public Identifier getBackgroundTexture() {
        return backgroundTexture != null ? backgroundTexture : DrawableHelper.OPTIONS_BACKGROUND_TEXTURE;
    }


    /**
     * Adds all outer fields (non-nested and in parent class) from a config to the resulting screen.
     * @param translated whether the fields' names on the GUI are translatable
     * @return self
     */
    public ScreenBuilder allOuter(boolean translated) {
        return allFromClass(this.parentConfig, translated);
    }

    /**
     * Adds all outer fields (non-nested and in parent class) from a config to the resulting screen.
     * @return self
     */
    public ScreenBuilder allOuter() {
        return allOuter(true);
    }

    /**
     * Adds all fields from an object to the resulting screen.
     * @param instance an instance of a possible nested class from your config
     * @param translated whether the fields' names on the GUI are translatable
     * @return self
     */
    public ScreenBuilder allFromClass(@NonNull Object instance, boolean translated) {
        Objects.requireNonNull(instance);
        List<ScreenEntry> entries = new ArrayList<>();
        for (var f : instance.getClass().getFields()) {
            entries.add(field(f.getName(), instance.getClass(), this.parentConfig, instance, translated));
        }
        return addEntries(entries);
    }

    /**
     * Adds all fields from an object to the resulting screen. Said fields will not be translatable.
     * @param instance an instance of a possible nested class from your config
     * @return self
     */
    public ScreenBuilder allFromClass(@NonNull Object instance) {
        return allFromClass(instance, true);
    }

    /**
     * @param name the field name
     * @param receiverClass the declaring class from the field
     * @param parent the parent config
     * @param instance the instance to get/set this field
     * @param translated whether the entry's name on the GUI is translatable
     * @return a new {@link draylar.omegaconfiggui.api.screen.ScreenEntry.FieldEntry}
     */
    public static ScreenEntry field(String name, Class<?> receiverClass, Config parent, Object instance, boolean translated) {
        return new ScreenEntry.FieldEntry(name, receiverClass, parent, instance, translated);
    }

    /**
     * @param name the field name
     * @param receiverClass the declaring class from the field
     * @param parent the parent config
     * @param instance the instance to get/set this field
     * @return a new {@link draylar.omegaconfiggui.api.screen.ScreenEntry.FieldEntry}
     */
    public static ScreenEntry field(String name, Class<?> receiverClass, Config parent, Object instance) {
        return field(name, receiverClass, parent, instance, true);
    }

    /**
     * @param screen a supplier of the screen this entry will resolve to
     * @param title the title of the entry on the screen
     * @return a new {@link draylar.omegaconfiggui.api.screen.ScreenEntry.SubScreenEntry}
     */
    public static ScreenEntry subScreenEntry(Supplier<Screen> screen, Text title) {
        return new ScreenEntry.SubScreenEntry(screen, title);
    }
}