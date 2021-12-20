package draylar.omegaconfiggui.mixin.modmenu;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(ModMenu.class)
public interface ModMenuAccessor {

    @Accessor
    static ImmutableMap<String, ConfigScreenFactory<?>> getConfigScreenFactories() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static void setConfigScreenFactories(ImmutableMap<String, ConfigScreenFactory<?>> factories) {
        throw new UnsupportedOperationException();
    }
}
