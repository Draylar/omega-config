package draylar.omegatest.config;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;
import org.jetbrains.annotations.Nullable;

public class CommentedNestedClassConfig implements Config {

    @Comment("Very nested test")
    public First first = new First();

    @Comment("True")
    public boolean test = false;

    public static class First {
        @Comment("Cucumbers")
        public boolean cucumbers = true;

        @Comment("InnerFirst")
        public InnerFirst innerFirst = new InnerFirst();

        public static class InnerFirst {
            @Comment("e")
            public boolean e = false;
        }
    }

    @Override
    public String getName() {
        return "commented-inner-classes";
    }

    @Override
    public @Nullable String getModid() {
        return "omega-config-gui";
    }

    @Override
    public String getExtension() {
        return "json5";
    }
}
