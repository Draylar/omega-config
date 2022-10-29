package draylar.omegatest;

import draylar.omegaconfig.OmegaConfig;
import draylar.omegatest.config.*;
import net.fabricmc.api.ModInitializer;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

public class OmegaTestMain implements ModInitializer {

    public static final SimpleConfigTest CONFIG = OmegaConfig.register(SimpleConfigTest.class);
    public static final StructuresConfigTest MO_CONFIG = OmegaConfig.register(StructuresConfigTest.class);
    public static final NestedConfigTest NESTED = OmegaConfig.register(NestedConfigTest.class);
    public static final ClassConfigTest CLASS = OmegaConfig.register(ClassConfigTest.class);
    public static final CommentedNestedClassConfig COMMENTED_NESTED_CLASS = OmegaConfig.register(CommentedNestedClassConfig.class);

    @Override
    public void onInitialize() {
        System.out.printf("Config value: %s%n", CONFIG.v);
        System.out.printf("Inner class value: %s%n", CONFIG.test.innerTest);
        
        // Verify config contents are exactly as expected
        Path expectedConfigsPath = Path.of("expected_configs");
        Path runPath = Path.of("config");
        verifyFilesEqual(expectedConfigsPath.resolve("test").resolve("nested.json"), runPath.resolve("test").resolve("nested.json"));
        verifyFilesEqual(expectedConfigsPath.resolve("class-config.json"), runPath.resolve("class-config.json"));
        verifyFilesEqual(expectedConfigsPath.resolve("commented-inner-classes.json5"), runPath.resolve("commented-inner-classes.json5"));
        verifyFilesEqual(expectedConfigsPath.resolve("mostructures-config-v2.json5"), runPath.resolve("mostructures-config-v2.json5"));
        verifyFilesEqual(expectedConfigsPath.resolve("test-config.json"), runPath.resolve("test-config.json"));
    }
    
    public void verifyFilesEqual(Path expected, Path actual) {
        System.out.printf("Verifying expected=[%s] is equal to actual=[%s]%n", expected, actual);
        try (InputStream file1 = getClass().getClassLoader().getResourceAsStream(expected.toString());
            InputStream file2 = new FileInputStream(actual.toString())) {
            if (!IOUtils.contentEquals(file1, file2)) {
                throw new RuntimeException(String.format("[%s] is not equal to [%s]", expected, actual));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
