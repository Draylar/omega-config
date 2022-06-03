package draylar.omegaconfiggui;

import draylar.omegaconfig.OmegaConfig;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.api.screen.OmegaModMenu;
import draylar.omegaconfiggui.api.screen.OmegaScreenFactory;
import draylar.omegaconfiggui.api.screen.ScreenBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class OmegaConfigGui {

    public static final Map<Config, OmegaScreenFactory<Screen>> REGISTERED_CONFIGURATIONS = new HashMap<>();
    public static boolean modMenuInitialized = false;

    public static <T extends Config> void registerConfigScreen(T config, OmegaScreenFactory<Screen> factory) {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            // Ensure the config has a valid modid.
            if (config.getModid() != null) {
                if (modMenuInitialized) {
                    OmegaModMenu.injectScreen(config, factory);
                } else {
                    REGISTERED_CONFIGURATIONS.put(config, factory);
                }
            } else {
                OmegaConfig.LOGGER.warn(String.format("Skipping config screen registration for '%s' - you must implement getModid() in your config class!", config.getName()));
            }
        }
    }

    /**
     * Registers a ModMenu configuration screen for the given {@link Config} instance.
     *
     * @param config registered config to create a ModMenu screen for
     * @param <T>    config type
     * 
     * @deprecated Use {@link #registerConfigScreen(Config, OmegaScreenFactory)}
     */
    @Deprecated
    public static <T extends Config> void registerConfigScreen(T config) {
        registerConfigScreen(config, getConfigScreenFactory(config));
    }

    /**
     * @param config Omega Config instance to create the screen factory for
     * @return a factory which provides new Cloth Config Lite {@link Screen} instances for the given {@link Config}.
     *
     * @deprecated Use {@link #registerConfigScreen(Config, OmegaScreenFactory)}
     */
    @Deprecated
    public static OmegaScreenFactory<Screen> getConfigScreenFactory(Config config) {
        return parent -> ScreenBuilder
                .create(
                        config,
                        Text.translatable(String.format("config.%s.%s", config.getModid(), config.getName())),
                        parent
                )
                .allOuter()
                .build();
    }

    public static Map<Config, OmegaScreenFactory<Screen>> getConfigScreenFactories() {
        return REGISTERED_CONFIGURATIONS;
    }
}
