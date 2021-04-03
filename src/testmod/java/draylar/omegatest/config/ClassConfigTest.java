package draylar.omegatest.config;

import draylar.omegaconfig.api.Config;

import java.util.Arrays;
import java.util.List;

public class ClassConfigTest implements Config {

    public List<TestClass> l = Arrays.asList(
            new TestClass()
    );

    @Override
    public String getName() {
        return "class-config";
    }

    public static class TestClass {
        public boolean a = true;
        public int b = 1;
    }
}
