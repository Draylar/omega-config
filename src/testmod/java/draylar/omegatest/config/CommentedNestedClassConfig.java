package draylar.omegatest.config;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;
import draylar.omegatest.config.nested.NestedConfig;

public class CommentedNestedClassConfig implements Config {

    @Comment("Very nested test")
    public First first = new First();

    @Comment("True")
    public boolean test = false;

    @Comment("First-Level")
    public VeryNested veryNested = new VeryNested();
    
    @Comment("Nested outer class")
    public NestedConfig nestedConfig = new NestedConfig();

    public static class First {
        @Comment("Cucumbers")
        public boolean cucumbers = true;

        @Comment("InnerFirst")
        public InnerFirst innerFirst = new InnerFirst();

        @Comment("InnerSecond")
        public InnerSecond innerSecond = new InnerSecond();

        public static class InnerFirst {
            @Comment("Number of Tomatoes")
            public int numberOfTomatoes = 10;
        }

        public static class InnerSecond {
            @Comment("Inner Second First")
            public InnerSecondFirst innerSecondFirst = new InnerSecondFirst();

            public static class InnerSecondFirst {
                @Comment("Olive Oil")
                public String oliveOil = "oliveOil";
            }
        }
    }

    public static class VeryNested {
        @Comment("Second-Level")
        public Very very = new Very();
        public static class Very {
            @Comment("Third-Level")
            public VeryVery veryVery = new VeryVery();
            public static class VeryVery {
                @Comment("Forth-Level")
                public VeryVeryVery veryVeryVery = new VeryVeryVery();
                public static class VeryVeryVery {
                    @Comment("Fifth-Level")
                    public VeryVeryVeryVery veryVeryVeryVery = new VeryVeryVeryVery();
                    public static class VeryVeryVeryVery {
                        @Comment("Sixth-Level")
                        public String last = "last";
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "commented-inner-classes";
    }

    @Override
    public String getExtension() {
        return "json5";
    }
}
