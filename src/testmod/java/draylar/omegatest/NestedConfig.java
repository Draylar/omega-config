package draylar.omegatest;

import draylar.omegaconfig.api.Config;

public class NestedConfig implements Config {

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
