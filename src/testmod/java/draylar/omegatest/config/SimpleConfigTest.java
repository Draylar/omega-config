package draylar.omegatest.config;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfig.api.Syncing;
import org.jetbrains.annotations.Nullable;

@Syncing
public class SimpleConfigTest implements Config {

    @Comment(value = "Hello!")
    public boolean v = false;

    @Comment(value = "I'm a double.")
    public double doubleTest = 0.0;

    public String stringTest = "Hello, world!";

    @Comment(value = "This is the first inner class test config.")
    public FirstTest firstTest = new FirstTest();

    @Comment(value = "This is the second inner class test config.")
    public SecondTest secondTest = new SecondTest();

    @Override
    public String getName() {
        return "test-config";
    }

    @Override
    public @Nullable String getModid() {
        return "omega-config-test";
    }


    public static class FirstTest {

        @Comment(value = "This is the value inside the class!")
        public boolean innerTest = false;

        @Comment(value = "Very neat indeed.")
        public String neatString = "neat";
    }

    public static class SecondTest {

        @Comment(value = "I'm very happy :)")
        public double happinessLevel = 83.7D;

        @Comment(value = "Bye!")
        public boolean w = false;
    }
}
