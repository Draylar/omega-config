package draylar.omegaconfiggui.api.screen;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.OmegaConfigGui;
import draylar.omegaconfiggui.mixin.modmenu.ModMenuAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class OmegaModMenu {

    private static final Map<Config, ConfigScreenFactory<Screen>> REGISTERED_CONFIGURATIONS = new HashMap<>();
    public static boolean modMenuInitialized = false;

    /**
     * Registers a ModMenu configuration screen for the given {@link Config} instance.
     *
     * @param config registered config to create a ModMenu screen for
     * @param <T>    config type
     */
    public static <T extends Config> void registerConfigScreen(T config) {
        if(FabricLoader.getInstance().isModLoaded("modmenu")) {
            OmegaScreenFactory<Screen> factory = OmegaConfigGui.getConfigScreenFactory(config);

            if(modMenuInitialized) {
                OmegaModMenu.injectScreen(config, factory::get);
            } else {
                addConfiguration(config, factory::get);
            }
        }
    }

    public static <T extends Config> void injectScreen(T config, ConfigScreenFactory<Screen> factory) {
        // they will suspect nothing
        ModMenuAccessor.setConfigScreenFactories(
                new ImmutableMap.Builder<String, ConfigScreenFactory<?>>()
                        .putAll(ModMenuAccessor.getConfigScreenFactories())
                        .put(config.getModid(), factory)
                        .build());
    }

    public static Map<Config, ConfigScreenFactory<Screen>> getRegisteredConfigurations() {
        return REGISTERED_CONFIGURATIONS;
    }

    public static void addConfiguration(Config config, ConfigScreenFactory<Screen> screen) {
        REGISTERED_CONFIGURATIONS.put(config, screen);
    }
}
