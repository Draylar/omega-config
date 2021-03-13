package draylar.omegaconfiggui.api.screen;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.mixin.ModMenuAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuHelper {

    public static <T extends Config> void injectScreen(T config, OmegaConfigScreenSupplier<T> factory) {
        // they will suspect nothing
        ModMenuAccessor.setConfigScreenFactories(
                new ImmutableMap.Builder<String, ConfigScreenFactory<?>>()
                        .putAll(ModMenuAccessor.getConfigScreenFactories())
                        .put(config.getModid(), factory::get)
                        .build());
    }
}
