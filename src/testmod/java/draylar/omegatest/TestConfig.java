package draylar.omegatest;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfig.api.Syncing;
import org.jetbrains.annotations.Nullable;

@Syncing
public class TestConfig implements Config {

    @Comment(value = "Hello!")
    boolean v = false;

    @Comment(value = "I'm a double.")
    double doubleTest = 0.0;

    String stringTest = "Hello, world!";

    @Comment(value = "This is an inner static class.")
    public Test test = new Test();

    public static class Test {

        @Comment(value = "This is the value inside the class!")
        public boolean innerTest = false;
    }

    @Override
    public String getName() {
        return "test-config";
    }

    @Override
    public @Nullable String getModid() {
        return "omega-config-test";
    }
}
