package draylar.omegaconfig.api;

import draylar.omegaconfig.OmegaConfig;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public interface Config {

    /**
     * Writes this configuration file instance to disk.
     * Useful for saving modified values during runtime.
     */
    default void save() {
        OmegaConfig.writeConfig((Class<Config>) getClass(), this);
    }

    default CompoundTag writeSyncingTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ConfigName", getName());

        // all config vs. individual fields
        if(Arrays.stream(getClass().getAnnotations()).anyMatch(annotation -> annotation instanceof Syncing)) {
            // write ALL fields to tag
            String json = OmegaConfig.GSON.toJson(this);
            tag.putString("Serialized", json);
            tag.putBoolean("AllSync", true);
        } else {
            // write all syncable fields to tag
            String json = OmegaConfig.SYNC_ONLY_GSON.toJson(this);
            tag.putString("Serialized", json);
            tag.putBoolean("AllSync", false);
        }

        return tag;
    }

    /**
     * @return  true if this {@link Config} has any values that should be synced to the client
     */
    default boolean hasAnySyncable() {
        boolean hasSyncingField = Arrays.stream(getClass().getDeclaredFields()).anyMatch(field -> Arrays.stream(field.getDeclaredAnnotations()).anyMatch(annotation -> annotation instanceof Syncing));
        boolean classSyncs = Arrays.stream(getClass().getDeclaredAnnotations()).anyMatch(annotation -> annotation instanceof Syncing);
        return hasSyncingField | classSyncs;
    }

    /**
     * Returns the name of this config, which is used for the name of the config file saved to disk, and syncing.
     *
     * <p>
     * The name returned by this method should generally follow Identifier conventions, but this is not enforced:
     * <ul>
     *     <li>Lowercase
     *     <li>No special characters ($, %, ^, etc.)
     *     <li>No spaces
     *
     * @return  the name of this config, which is used for the name of the config file saved to disk.
     */
    String getName();

    /**
     * Returns the modid associated with this configuration class.
     *
     * <p>
     * This functionality is used for libraries like ModMenu, which depend on
     *  modids for configuration screen instances in their menu.
     * If you are intending for this config to have a ModMenu config
     *  screen, this method should return the modid specified in your fabric.mod.json.
     *
     * If this method is not overridden, null will be returned, which
     *  means this config is not explicitly associated with any particular mod.
     *
     * @return  the modid of the mod associated with this config, or null if none was specified
     */
    @Nullable
    default String getModid() {
        return null;
    }

    default boolean hasMenu() {
        return true;
    }
}
