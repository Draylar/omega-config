package draylar.omegaconfiggui;

import draylar.omegaconfig.api.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class OmegaConfigGui {

    /**
     * Registers a ModMenu configuration screen for the given {@link Config} instance.
     *
     * @param config registered config to create a ModMenu screen for
     * @param <T>    config type
     */
    public static <T extends Config> void registerConfigScreen(T config) {
        if(FabricLoader.getInstance().isModLoaded("modmenu")) {
//            ModMenuHelper.injectScreen(config, parent -> new OmegaConfigScreen<>(config, parent));
        }
    }
}
