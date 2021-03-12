package draylar.omegaconfig.mixin;

import draylar.omegaconfig.OmegaConfig;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfig.api.Syncing;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(MinecraftClient.class)
public class ClientMixin {

    @Unique
    private final List<Config> savedClientConfig = new ArrayList<>(); // stored config from before sync is applied

    @Inject(
            method = "<init>",
        at = @At("RETURN"))
    private void onReturn(RunArgs args, CallbackInfo ci) {
        ClientSidePacketRegistry.INSTANCE.register(OmegaConfig.CONFIG_SYNC_PACKET, (context, buffer) -> {
            CompoundTag tag = buffer.readCompoundTag();
            savedClientConfig.clear();

            context.getTaskQueue().execute(() -> {
                if(tag != null && tag.contains("Configurations")) {
                    ListTag list = tag.getList("Configurations", NbtType.COMPOUND);
                    list.forEach(compound -> {
                        CompoundTag syncedConfiguration = (CompoundTag) compound;
                        String name = syncedConfiguration.getString("ConfigName");
                        String json = syncedConfiguration.getString("Serialized");
                        boolean allSync = syncedConfiguration.getBoolean("AllSync");

                        // find configuration class by name
                        for (Config config : OmegaConfig.getRegisteredConfigurations()) {
                            if (config.getFileName().equals(name)) {
                                // bring values from server to object
                                Config server = OmegaConfig.GSON.fromJson(json, config.getClass());

                                // deep-copy original config & save it for a restore later
                                Config cachedClient = OmegaConfig.GSON.fromJson(OmegaConfig.GSON.toJson(config), config.getClass());
                                savedClientConfig.add(cachedClient);

                                // locate all fields that differ between the client and server, assign values from server to client (this will mutate the stored object)
                                for (Field field : server.getClass().getDeclaredFields()) {
                                    if(allSync || Arrays.stream(field.getAnnotations()).anyMatch(annotation -> annotation instanceof Syncing)) {
                                        try {
                                            field.setAccessible(true);
                                            Object serverValue = field.get(server);
                                            field.set(config, serverValue);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                break;
                            }
                        }
                    });
                }
            });
        });
    }

    @Inject(
            method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V",
            at = @At("RETURN"))
    private void restoreConfigurations(Screen screen, CallbackInfo ci) {
        for(Config config : savedClientConfig) {
            for(Config potentiallySynced : OmegaConfig.getRegisteredConfigurations()) {
                if(config.getFileName().equals(potentiallySynced.getFileName())) {
                    boolean allConfigSyncs = Arrays.stream(config.getClass().getAnnotations()).anyMatch(annotation -> annotation instanceof Syncing);

                    // mutate object in registered configurations
                    for (Field field : config.getClass().getDeclaredFields()) {
                        if(allConfigSyncs || Arrays.stream(field.getAnnotations()).anyMatch(annotation -> annotation instanceof Syncing)) {
                            try {
                                field.setAccessible(true);
                                Object preSyncValue = field.get(config);
                                field.set(potentiallySynced, preSyncValue);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        savedClientConfig.clear();
    }
}
