package draylar.omegaconfig.api.screen.widget;

import draylar.omegaconfig.api.screen.widget.supplier.BooleanWidgetSupplier;
import draylar.omegaconfig.api.screen.widget.supplier.DoubleWidgetSupplier;
import draylar.omegaconfig.api.screen.widget.supplier.StringWidgetSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TypeWidgets {

    public static final Map<Class<?>, WidgetSupplier<?, ?>> CLASS_WIDGETS = new HashMap<>();
    public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_BOXED = new HashMap<>();

    static {
        CLASS_WIDGETS.put(Double.class, new DoubleWidgetSupplier());
        CLASS_WIDGETS.put(Boolean.class, new BooleanWidgetSupplier());
        CLASS_WIDGETS.put(String.class, new StringWidgetSupplier());

        // primitive -> boxed
        PRIMITIVE_TO_BOXED.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_BOXED.put(byte.class, Byte.class);
        PRIMITIVE_TO_BOXED.put(char.class, Character.class);
        PRIMITIVE_TO_BOXED.put(double.class, Double.class);
        PRIMITIVE_TO_BOXED.put(float.class, Float.class);
        PRIMITIVE_TO_BOXED.put(int.class, Integer.class);
        PRIMITIVE_TO_BOXED.put(long.class, Long.class);
        PRIMITIVE_TO_BOXED.put(short.class, Short.class);
        PRIMITIVE_TO_BOXED.put(void.class, Void.class);
    }

    @Nullable
    public static <T> WidgetSupplier<T, ?> get(Class<T> typeClass) {
        return (WidgetSupplier<T, ?>) CLASS_WIDGETS.get(unbox(typeClass));
    }

    public static Class<?> unbox(Class<?> c) {
        return PRIMITIVE_TO_BOXED.getOrDefault(c, c);
    }

    private TypeWidgets() {
        // NO-OP
    }
}
