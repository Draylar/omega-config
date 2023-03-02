package draylar.omegatest;

import draylar.omegaconfiggui.OmegaConfigGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class OmegaTestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OmegaConfigGui.registerConfigScreen(OmegaTestMain.CONFIG);

        HudRenderCallback.EVENT.register((stack, delta) -> {
            MinecraftClient.getInstance().textRenderer.draw(stack, Text.literal(String.valueOf(OmegaTestMain.CONFIG.v)), 15, 15, 0xffffff);
            MinecraftClient.getInstance().textRenderer.draw(stack, Text.literal(String.valueOf(OmegaTestMain.CONFIG.doubleTest)), 15, 30, 0xffffff);
        });
    }
}
