package draylar.omegatest.config;

import draylar.omegaconfig.api.Config;

public class ClassConfigTest implements Config {

    public TestClass[] testClass = {
            new TestClass()
    };

    @Override
    public String getName() {
        return "class-config";
    }

    public static class TestClass {
        public boolean a = true;
        public int b = 1;
    }
}
