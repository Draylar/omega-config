package draylar.omegaconfig.mixin;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModMenu.class)
public interface ModMenuAccessor {
    @Accessor
    static ImmutableMap<String, ConfigScreenFactory<?>> getConfigScreenFactories() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static void setConfigScreenFactories(ImmutableMap<String, ConfigScreenFactory<?>> configScreenFactories) {
        throw new UnsupportedOperationException();
    }
}
