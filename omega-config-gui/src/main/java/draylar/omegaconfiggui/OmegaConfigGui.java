package draylar.omegaconfiggui;

import draylar.omegaconfig.OmegaConfig;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.api.screen.OmegaScreenFactory;
import me.shedaniel.clothconfiglite.api.ConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Environment(EnvType.CLIENT)
public class OmegaConfigGui {

    /**
     * Returns a factory which provides new Cloth Config Lite {@link Screen} instances for the given {@link Config}.
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
}
