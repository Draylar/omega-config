package draylar.omegatest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class OmegaTestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((context, delta) -> {
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            context.drawText(tr, Text.literal(String.valueOf(OmegaTestMain.CONFIG.v)), 15, 15, 0xffffff, true);
            context.drawText(tr, Text.literal(String.valueOf(OmegaTestMain.CONFIG.doubleTest)), 15, 25, 0xffffff, true);
        });
    }
}
