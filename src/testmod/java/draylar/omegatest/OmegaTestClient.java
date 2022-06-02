package draylar.omegatest;

import draylar.omegaconfiggui.OmegaConfigGui;
import draylar.omegaconfiggui.api.screen.ScreenBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class OmegaTestClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OmegaConfigGui.registerConfigScreen(OmegaTestMain.CONFIG, parent -> {
            var builder = ScreenBuilder.create(
                    OmegaTestMain.CONFIG,
                    Text.of(OmegaTestMain.CONFIG.getName()),
                    parent
            );

            builder.newBuilderWithSelfParent(Text.of("Test #1"))
                    .allFromClass(OmegaTestMain.CONFIG.firstTest);
            builder.newBuilderWithSelfParent(Text.of("Test #2"))
                    .allFromClass(OmegaTestMain.CONFIG.secondTest);
            builder.allOuter();

            return builder.toScreen();
        });

        // Comment the previous code to test this
        OmegaConfigGui.registerConfigScreen(OmegaTestMain.COMMENTED_NESTED_CLASS, parent -> {
            var builder = ScreenBuilder.create(
                    OmegaTestMain.COMMENTED_NESTED_CLASS,
                    Text.of(OmegaTestMain.COMMENTED_NESTED_CLASS.getName()),
                    parent
            );
            builder.newBuilderWithSelfParent(Text.of("First"))
                    .newBuilderWithSelfParent(Text.of("InnerFirst"))
                        .allFromClass(OmegaTestMain.COMMENTED_NESTED_CLASS.first.innerFirst)
                        .getParent()
                    .allFromClass(OmegaTestMain.COMMENTED_NESTED_CLASS.first);
            builder.allOuter();

            return builder.toScreen();
        });

        HudRenderCallback.EVENT.register((stack, delta) -> {
            MinecraftClient.getInstance().textRenderer.draw(stack, Text.of(String.valueOf(OmegaTestMain.CONFIG.v)), 15, 15, 0xffffff);
            MinecraftClient.getInstance().textRenderer.draw(stack, Text.of(String.valueOf(OmegaTestMain.CONFIG.doubleTest)), 15, 30, 0xffffff);
        });
    }
}
