package draylar.omegatest.config;

import draylar.omegaconfig.api.Config;

import java.util.Arrays;
import java.util.List;

public class ClassConfigTest implements Config {

    public List<TestClass> l = Arrays.asList(
            new TestClass(true, 1, "hello")
    );

    @Override
    public String getName() {
        return "class-config";
    }

    public static class TestClass {
        public final boolean a;
        public final int b;
        public String c;

        public TestClass(boolean a, int b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
