package draylar.omegaconfig;

import net.fabricmc.api.ModInitializer;

public class OmegaConfigTest implements ModInitializer {

    public static final TestConfig CONFIG = OmegaConfig.register(TestConfig.class);

    @Override
    public void onInitialize() {
        System.out.printf("Config value: %s%n", CONFIG.v);
        System.out.printf("Inner class value: %s%n", CONFIG.test.innerTest);
    }
}
