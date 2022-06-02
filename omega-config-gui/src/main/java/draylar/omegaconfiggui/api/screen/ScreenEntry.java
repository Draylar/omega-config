package draylar.omegaconfiggui.api.screen;

import draylar.omegaconfig.api.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface ScreenEntry {
    Map<Class<?>, Object> CACHED_DEFAULTS = new HashMap<>();

    Text getName();

    record FieldEntry(String name, Class<?> receiverClass, Config parent, Object instance) implements ScreenEntry {
        @Override
        public Text getName() {
            return Text.of(name);
        }

        public Object getDefault() {
            return CACHED_DEFAULTS.computeIfAbsent(instance.getClass(), c -> {
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("No valid constructor found for: " + c, e);
                }
            });
        }
    }

    record SubScreenEntry(Supplier<Screen> screen, Text title) implements ScreenEntry {
        @Override
        public Text getName() {
            return title;
        }
    }
}
