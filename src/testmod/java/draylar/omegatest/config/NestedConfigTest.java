package draylar.omegatest.config;

import draylar.omegaconfig.api.Config;

public class NestedConfigTest implements Config {

    public boolean test = false;

    @Override
    public String getName() {
        return "nested";
    }

    @Override
    public String getDirectory() {
        return "test";
    }
}
