package draylar.omegaconfig.api.screen;

import draylar.omegaconfig.api.Config;
import net.minecraft.client.gui.screen.Screen;

public interface OmegaConfigScreenSupplier<T extends Config> {
    OmegaConfigScreen<T> get(Screen parent);
}
