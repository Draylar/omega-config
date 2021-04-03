package draylar.omegatest;

import draylar.omegaconfig.OmegaConfig;
import draylar.omegatest.config.ClassConfigTest;
import draylar.omegatest.config.NestedConfigTest;
import draylar.omegatest.config.StructuresConfigTest;
import draylar.omegatest.config.SimpleConfigTest;
import net.fabricmc.api.ModInitializer;

public class OmegaTestMain implements ModInitializer {

    public static final SimpleConfigTest CONFIG = OmegaConfig.register(SimpleConfigTest.class);
    public static final StructuresConfigTest MO_CONFIG = OmegaConfig.register(StructuresConfigTest.class);
    public static final NestedConfigTest NESTED = OmegaConfig.register(NestedConfigTest.class);
    public static final ClassConfigTest CLASS = OmegaConfig.register(ClassConfigTest.class);

    @Override
    public void onInitialize() {
        System.out.printf("Config value: %s%n", CONFIG.v);
        System.out.printf("Inner class value: %s%n", CONFIG.test.innerTest);
    }
}
