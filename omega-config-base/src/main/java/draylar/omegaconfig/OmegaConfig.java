package draylar.omegaconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfig.gson.SyncableExclusionStrategy;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class OmegaConfig implements ModInitializer {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Identifier CONFIG_SYNC_PACKET = new Identifier("omegaconfig", "sync");
    public static final Gson SYNC_ONLY_GSON = new GsonBuilder().addSerializationExclusionStrategy(new SyncableExclusionStrategy()).setPrettyPrinting().create();
    public static final Logger LOGGER = LogManager.getLogger();
    private static final List<Config> REGISTERED_CONFIGURATIONS = new ArrayList<>();

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> server.execute(() -> {
            PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());

            // list of configurations that are synced
            NbtCompound root = new NbtCompound();
            NbtList configurations = new NbtList();

            // Iterate over each configuration.
            // Find values that should be synced and send the value over.
            OmegaConfig.getRegisteredConfigurations().forEach(config -> {
                if (config.hasAnySyncable()) {
                    configurations.add(config.writeSyncingTag());
                }
            });

            // save to packet and send to user
            root.put("Configurations", configurations);
            packet.writeNbt(root);
            handler.sendPacket(ServerPlayNetworking.createS2CPacket(CONFIG_SYNC_PACKET, packet));
        }));
    }

    public static <T extends Config> T register(Class<T> configClass) {
        try {
            // Attempt to instantiate a new instance of this class.
            T config = configClass.getDeclaredConstructor().newInstance();

            // Exceptions will have been thrown at this point.
            // We want to provide access to the config as soon as it is created, so we:
            //    1. serialize to disk if the config does not already exist
            //    2. read from disk if it does exist
            if (!configExists(config)) {
                config.save();
                REGISTERED_CONFIGURATIONS.add(config);
            } else {
                try {
                    // Read from the disk config file to populate the correct values into our config object.
                    List<String> lines = Files.readAllLines(getConfigPath(config));
                    lines.removeIf(line -> line.trim().startsWith("//"));
                    StringBuilder res = new StringBuilder();
                    lines.forEach(res::append);
                    T object = GSON.fromJson(res.toString(), configClass);

                    // re-write the config to add new values
                    object.save();
                    REGISTERED_CONFIGURATIONS.add(object);
                    return object;
                } catch (Exception e) {
                    LOGGER.error(e);
                    LOGGER.info(String.format("Encountered an error while reading %s config, falling back to default values.", config.getName()));
                    LOGGER.info(String.format("If this problem persists, delete the config file %s and try again.", config.getName() + "." + config.getExtension()));
                }
            }

            return config;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
            throw new RuntimeException("No valid constructor found for: " + configClass.getName());
        }
    }

    public static <T extends Config> void writeConfig(Class<T> configClass, T instance) {
        // Write the config to disk with the default values.
        String json = GSON.toJson(instance);

        // Cursed time.
        List<String> lines = new ArrayList<>(Arrays.asList(json.split("\n")));
        Map<Integer, String> insertions = new TreeMap<>();
        Map<String, String> keyToComments = populateKeyPathToComments(configClass);

        // Find areas we should insert comments into...

        // Prefix stack that keeps track of the nested json element's parents
        Deque<String> prefix = new ArrayDeque<>();
        int currIndent = 1;  // Past the root, indents start at 1
        for (int i = 0; i < lines.size(); i++) {
            String at = lines.get(i);
            String trimmed = at.trim();

            // Exited a json structure, pop the prefix stack
            int indents = countGsonIndents(at);
            if (indents <= currIndent) {
                if (!prefix.isEmpty()) {
                    prefix.removeLast();
                }
            }

            // Fields start with a quote "
            if (trimmed.startsWith("\"")) {
                // Remove quotes around the "key"
                String key = trimmed.substring(1, trimmed.indexOf("\"", 1));

                // Push onto the prefix stack, appending onto the previous prefix if nested, else just the key itself
                if (indents >= currIndent) {
                    if (prefix.isEmpty()) {
                        prefix.addLast(key);
                    } else {
                        prefix.addLast(prefix.peekLast() + "." + key);
                    }
                }

                String startingWhitespace = "  ".repeat(indents);

                for (Map.Entry<String, String> entry : keyToComments.entrySet()) {
                    String comment = entry.getValue();
                    // Check if we should insert comment
                    if (entry.getKey().equals(prefix.peekLast())) {
                        if (comment.contains("\n")) {
                            comment = startingWhitespace + "//" + String.join(String.format("\n%s//", startingWhitespace), comment.split("\n"));
                        } else {
                            comment = String.format("%s//%s", startingWhitespace, comment);
                        }
                        insertions.put(i + insertions.size(), comment);
                        break;
                    }
                }
            }

            currIndent = indents;
        }

        // insertions -> list
        for (Map.Entry<Integer, String> entry : insertions.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            lines.add(key, value);
        }

        // list -> string
        StringBuilder res = new StringBuilder();
        lines.forEach(str -> res.append(String.format("%s%n", str)));

        try {
            Path configPath = getConfigPath(instance);
            configPath.toFile().getParentFile().mkdirs();
            Files.write(configPath, res.toString().getBytes());
        } catch (IOException ioException) {
            LOGGER.error(ioException);
            LOGGER.info(String.format("Write error, using default values for config %s.", configClass));
        }
    }

    /**
     * Fetches comments from Json key "paths".  A path is the key itself if at the root top-level, else a period
     * delimited path from the root to the key.
     * 
     * For example, if this were our commented json:
     *    ```
     *     {
     *         // first comment
     *         "first": 1,
     *         // nested comment
     *         "nested": {
     *             // second comment
     *             "second": 2,
     *             // more nested comment
     *             "morenested": {
     *                 // third comment
     *                 "third": 3
     *             }
     *         }
     *     }
     *    ```
     *    
     * The returned map would be:
     *    ```
     *    "first" => "first comment"
     *    "nested" => "nested comment"
     *    "nested.second" => "second comment"
     *    "nested.morenested" => "more nested comment"
     *    "nested.morenested.third" => "third comment"
     *    ```
     * 
     * @param root Root class to recursively map field path keys to comments
     * @return Mapping from field path keys to comments
     */
    public static Map<String, String> populateKeyPathToComments(Class<?> root) {
        Map<String, String> keyPathToComments = new HashMap<>();
        populateKeyPathToComments(root, "", keyPathToComments, new HashSet<>());
        return keyPathToComments;
    }

    /**
     * Counts number of indents.  Gson uses double space as the default indent.
     */
    public static int countGsonIndents(String s) {
        String indent = "  ";
        int count = 0;
        while (s.startsWith(indent)) {
            count++;
            s = s.substring(indent.length());
        }
        return count;
    }

    /**
     * Finds all comments recursively.
     */
    private static void populateKeyPathToComments(Class<?> clazz, String prefix, Map<String, String> keyPathToComments,
                                                  Set<Class<?>> visited) {
        if (visited.contains(clazz)) {
            return;
        }

        visited.add(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            String comment = getFieldComment(field);
            String key = prefix + field.getName();
            // populate top-level key -> comments map
            if (comment != null) {
                keyPathToComments.put(key, comment);
            }

            // Recurse on field classes
            populateKeyPathToComments(field.getType(), key + ".", keyPathToComments, visited);
        }
    }

    private static String getFieldComment(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();

        // Find comment
        for (Annotation annotation : annotations) {
            if (annotation instanceof Comment) {
                return ((Comment) annotation).value();
            }
        }

        return null;
    }

    public static Path getConfigPath(Config config) {
        return Paths.get(FabricLoader.getInstance().getConfigDir().toString(), config.getDirectory(), String.format("%s.%s", config.getName(), config.getExtension()));
    }

    public static boolean configExists(Config config) {
        return Files.exists(getConfigPath(config));
    }

    public static List<Config> getRegisteredConfigurations() {
        return REGISTERED_CONFIGURATIONS;
    }
}
