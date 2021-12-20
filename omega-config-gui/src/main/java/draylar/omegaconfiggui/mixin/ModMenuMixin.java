package draylar.omegaconfiggui.mixin;

import com.terraformersmc.modmenu.ModMenu;
import draylar.omegaconfiggui.api.screen.OmegaModMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ModMenu.class)
public class ModMenuMixin {

    @Inject(method = "onInitializeClient", at = @At("RETURN"), remap = false)
    private void addOmegaConfigurationScreens(CallbackInfo ci) {
        OmegaModMenu.modMenuInitialized = true;

        // Add loaded configuration screens to mod menu.
        OmegaModMenu.getRegisteredConfigurations().forEach(OmegaModMenu::injectScreen);
    }
}
