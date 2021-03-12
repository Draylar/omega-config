package draylar.omegaconfiggui;

import draylar.omegaconfig.api.Config;
import draylar.omegaconfiggui.api.screen.ModMenuHelper;
import draylar.omegaconfiggui.api.screen.OmegaConfigScreen;
import draylar.omegaconfiggui.api.screen.OmegaConfigScreenSupplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class OmegaConfigGui implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

    }

    public static <T extends Config> void registerConfigScreen(T config) {
        registerConfigScreen(config, parent -> new OmegaConfigScreen<>(config, parent));
    }

    public static <T extends Config> void registerConfigScreen(T config, OmegaConfigScreenSupplier<T> screenFactory) {
        if(FabricLoader.getInstance().isModLoaded("modmenu")) {
            ModMenuHelper.injectScreen(config, screenFactory);
        }
    }
}
