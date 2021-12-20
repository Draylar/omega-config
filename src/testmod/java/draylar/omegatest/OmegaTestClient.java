package draylar.omegatest;

import draylar.omegaconfiggui.api.screen.OmegaModMenu;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class OmegaTestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OmegaModMenu.registerConfigScreen(OmegaTestMain.CONFIG);

        HudRenderCallback.EVENT.register((stack, delta) -> {
            MinecraftClient.getInstance().textRenderer.draw(stack, new LiteralText(String.valueOf(OmegaTestMain.CONFIG.v)), 15, 15, 0xffffff);
            MinecraftClient.getInstance().textRenderer.draw(stack, new LiteralText(String.valueOf(OmegaTestMain.CONFIG.doubleTest)), 15, 30, 0xffffff);
        });
    }
}
