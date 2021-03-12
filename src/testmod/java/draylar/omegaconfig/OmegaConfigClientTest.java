package draylar.omegaconfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class OmegaConfigClientTest implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OmegaConfigClient.registerConfigScreen(OmegaConfigTest.CONFIG);

        HudRenderCallback.EVENT.register((stack, delta) -> {
            MinecraftClient.getInstance().textRenderer.draw(stack, new LiteralText(String.valueOf(OmegaConfigTest.CONFIG.v)), 15, 15, 0xffffff);
            MinecraftClient.getInstance().textRenderer.draw(stack, new LiteralText(String.valueOf(OmegaConfigTest.CONFIG.doubleTest)), 15, 30, 0xffffff);
        });
    }
}
