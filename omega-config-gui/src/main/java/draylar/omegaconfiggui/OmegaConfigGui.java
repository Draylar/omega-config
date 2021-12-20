package draylar.omegaconfiggui;

import draylar.omegaconfig.OmegaConfig;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.api.screen.OmegaModMenu;
import draylar.omegaconfiggui.api.screen.OmegaScreenFactory;
import me.shedaniel.clothconfiglite.api.ConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class OmegaConfigGui {

    private static final Map<Config, OmegaScreenFactory<Screen>> REGISTERED_CONFIGURATIONS = new HashMap<>();
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
                OmegaModMenu.injectScreen(config, factory);
            } else {
                REGISTERED_CONFIGURATIONS.put(config, factory);
            }
        }
    }

    /**
     * Returns a factory which provides new Cloth Config Lite {@link Screen} instances for the given {@link Config}.
     *
     * @param config Omega Config instance to create the screen factory for
     * @return a factory which provides new Cloth Config Lite {@link Screen} instances for the given {@link Config}.
     */
    public static OmegaScreenFactory<Screen> getConfigScreenFactory(Config config) {
        return parent -> {
            try {
                Config defaultConfig = config.getClass().getDeclaredConstructor().newInstance();
                ConfigScreen screen = ConfigScreen.create(new LiteralText(config.getName()), parent);

                // Fields
                for (Field field : config.getClass().getDeclaredFields()) {
                    try {
                        screen.add(new LiteralText(field.getName()), field.get(config), () -> {
                            try {
                                return field.get(defaultConfig);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            return 0.0d;
                        }, newValue -> {
                            try {
                                field.set(config, newValue);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            config.save();
                        });
                    } catch (IllegalAccessException | IllegalArgumentException exception) {
                        // ignored
                    }
                }

                return screen.get();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                OmegaConfig.LOGGER.error(String.format("Configuration class for mod %s must have a no-argument constructor for retrieving default values.", config.getModid()));
            }

            // todo: is this a bad idea
            return null;
        };
    }

    public static Map<Config, OmegaScreenFactory<Screen>> getConfigScreenFactories() {
        return REGISTERED_CONFIGURATIONS;
    }
}
