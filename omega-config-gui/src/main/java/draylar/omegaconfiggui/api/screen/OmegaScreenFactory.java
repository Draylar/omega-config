package draylar.omegaconfiggui.api.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

@Environment(EnvType.CLIENT)
public interface OmegaScreenFactory<T extends Screen> {
    T get(Screen parent);
}
