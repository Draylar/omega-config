package draylar.omegaconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;
import draylar.omegaconfig.gson.SyncableExclusionStrategy;
import draylar.omegaconfig.exception.NoValidConstructorException;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Config> REGISTERED_CONFIGURATIONS = new ArrayList<>();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Identifier CONFIG_SYNC_PACKET = new Identifier("omegaconfig", "sync");
    public static final Gson SYNC_ONLY_GSON = new GsonBuilder().addSerializationExclusionStrategy(new SyncableExclusionStrategy()).setPrettyPrinting().create();

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.execute(() -> {
                PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());

                // list of configurations that are synced
                CompoundTag root = new CompoundTag();
                ListTag configurations = new ListTag();

                // Iterate over each configuration.
                // Find values that should be synced and send the value over.
                OmegaConfig.getRegisteredConfigurations().forEach(config -> {
                    if(config.hasAnySyncable()) {
                        configurations.add(config.writeSyncingTag());
                    }
                });

                // save to packet and send to user
                root.put("Configurations", configurations);
                packet.writeCompoundTag(root);
                handler.sendPacket(ServerPlayNetworking.createS2CPacket(CONFIG_SYNC_PACKET, packet));
            });
        });
    }

    public static <T extends Config> T register(Class<T> configClass) {
        try {
            // Attempt to instantiate a new instance of this class.
            T config = configClass.getDeclaredConstructor().newInstance();

            // Exceptions will have been thrown at this point.
            // We want to provide access to the config as soon as it is created, so we:
            //    1. serialize to disk if the config does not already exist
            //    2. read from disk if it does exist
            if(!configExists(config)) {
                writeConfig(configClass, config);
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
                    writeConfig(configClass, object);
                    REGISTERED_CONFIGURATIONS.add(object);
                    return object;
                } catch (IOException ioException) {
                    LOGGER.error(ioException);
                    LOGGER.info(String.format("Read error, using default values for config %s.", configClass.toString()));
                }
            }

            return config;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new NoValidConstructorException();
        }
    }

    public static  <T extends Config> void  writeConfig(Class<T> configClass, T instance) {
        // Write the config to disk with the default values.
        String json = GSON.toJson(instance);

        // Cursed time.
        List<String> lines = new ArrayList<>(Arrays.asList(json.split("\n")));
        Map<Integer, String> insertions = new HashMap<>();
        Map<String, String> keyToComments = new HashMap<>();

        // populate key -> comments map
        for(Field field : configClass.getDeclaredFields()) {
            addFieldComments(field, keyToComments);
        }

        // get inner-class fields
        // TODO: recursively get inner classes?
        for(Class<?> innerClass : configClass.getDeclaredClasses()) {
            for(Field field : innerClass.getDeclaredFields()) {
                addFieldComments(field, keyToComments);
            }
        }

        // Find areas we should insert comments into...
        for (int i = 0; i < lines.size(); i++) {
            String at = lines.get(i);

            // Check if we should insert comment
            for (Map.Entry<String, String> entry : keyToComments.entrySet()) {
                String key = entry.getKey();
                String comment = entry.getValue();

                if (at.trim().startsWith(String.format("\"%s\"", key))) {
                    insertions.put(i + insertions.size(), String.format("%s//%s", getStartingWhitespace(at), comment));
                    break;
                }
            }
        }

        // insertions -> list
        insertions.forEach(lines::add);

        // list -> string
        StringBuilder res = new StringBuilder();
        lines.forEach(str -> res.append(String.format("%s%n", str)));

        try {
            Files.write(getConfigPath(instance), res.toString().getBytes());
        } catch (IOException ioException) {
            LOGGER.error(ioException);
            LOGGER.info(String.format("Write error, using default values for config %s.", configClass.toString()));
        }
    }

    private static void addFieldComments(Field field, Map<String, String> keyToComments) {
        String fieldName = field.getName();
        Annotation[] annotations = field.getDeclaredAnnotations();

        // Find comment
        for (Annotation annotation : annotations) {
            if(annotation instanceof Comment) {
                keyToComments.put(fieldName, ((Comment) annotation).value());
                break;
            }
        }
    }

    /**
     * Returns a string with the left-side whitespace characters of the given input, up till the first non-whitespace character.
     *
     * <p>
     * "   hello" -> "   "
     * "p" -> ""
     * " p" -> " "
     *
     * @param input  input to retrieve whitespaces from
     * @return       starting whitespaces from the given input
     */
    private static String getStartingWhitespace(String input) {
        int index = -1;

        char[] chars = input.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            char at = chars[i];

            if(at != ' ') {
                index = i;
                break;
            }
        }

        if(index != -1) {
            return input.substring(0, index);
        } else {
            return "";
        }
    }

    public static Path getConfigPath(Config config) {
        return Paths.get(FabricLoader.getInstance().getConfigDir().toString(), String.format("%s.json", config.getName()));
    }

    public static boolean configExists(Config config) {
        return Files.exists(getConfigPath(config));
    }

    public static List<Config> getRegisteredConfigurations() {
        return REGISTERED_CONFIGURATIONS;
    }
}
