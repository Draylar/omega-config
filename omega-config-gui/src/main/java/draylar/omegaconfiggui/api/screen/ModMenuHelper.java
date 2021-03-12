package draylar.omegaconfiggui.api.screen;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.mixin.ModMenuAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ModMenuHelper {

    public static <T extends Config> void injectScreen(T config, OmegaConfigScreenSupplier<T> factory) {
        // collect existing configurations
        ImmutableMap<String, ConfigScreenFactory<?>> configScreenFactories = ModMenuAccessor.getConfigScreenFactories();
        Map<String, ConfigScreenFactory<?>> nMap = new HashMap<>();
        nMap.putAll(configScreenFactories);

        // add our factory
        nMap.put(config.getModid(), factory::get);

        // they will suspect nothing
        ModMenuAccessor.setConfigScreenFactories(ImmutableMap.copyOf(nMap));
    }
}
