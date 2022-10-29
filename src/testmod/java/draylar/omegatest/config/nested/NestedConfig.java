package draylar.omegatest.config.nested;

import draylar.omegaconfig.api.Comment;

/**
 * Nested config, but not an inner class.
 */
public class NestedConfig {
    
    @Comment("Nested test")
    public boolean test = false;
    
    @Comment("Nested inner static class")
    public NestedInner nestedInner = new NestedInner();

    /**
     * Make sure nested inner static classes still work at multiple levels.
     */
    public static class NestedInner {
        @Comment("NestedInner")
        public String spoorn = "owl";
    }
}
