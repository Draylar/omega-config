package draylar.omegatest;

import draylar.omegaconfiggui.OmegaConfigGui;
import draylar.omegaconfiggui.api.screen.ScreenBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class OmegaTestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OmegaConfigGui.registerConfigScreen(OmegaTestMain.CONFIG, parent -> {
            var builder = ScreenBuilder.create(
                    OmegaTestMain.CONFIG,
                    new LiteralText(OmegaTestMain.CONFIG.getName()),
                    parent
            );

            builder.newBuilderWithSelfParent(new LiteralText("Nested"))
                    .allFromClass(OmegaTestMain.CONFIG.test);
            builder.allOuter();

            return builder.toScreen();
        });

        OmegaConfigGui.registerConfigScreen(OmegaTestMain.COMMENTED_NESTED_CLASS, parent -> {
            var builder = ScreenBuilder.create(
                    OmegaTestMain.COMMENTED_NESTED_CLASS,
                    new LiteralText(OmegaTestMain.COMMENTED_NESTED_CLASS.getName()),
                    parent
            );
            builder.newBuilderWithSelfParent(new LiteralText("First"))
                    .newBuilderWithSelfParent(new LiteralText("InnerFirst"))
                        .allFromClass(OmegaTestMain.COMMENTED_NESTED_CLASS.first.innerFirst)
                        .getParent()
                    .allFromClass(OmegaTestMain.COMMENTED_NESTED_CLASS.first);
            builder.allOuter();

            return builder.toScreen();
        });

        HudRenderCallback.EVENT.register((stack, delta) -> {
            MinecraftClient.getInstance().textRenderer.draw(stack, new LiteralText(String.valueOf(OmegaTestMain.CONFIG.v)), 15, 15, 0xffffff);
            MinecraftClient.getInstance().textRenderer.draw(stack, new LiteralText(String.valueOf(OmegaTestMain.CONFIG.doubleTest)), 15, 30, 0xffffff);
        });
    }
}
