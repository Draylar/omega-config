package draylar.omegaconfig.api;

import draylar.omegaconfig.OmegaConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public interface Config {

    /**
     * Writes this configuration file instance to disk.
     * Useful for saving modified values during runtime.
     */
    default void save() {

    }

    /**
     * @return  an instance of this Config class with all default values.
     */
    default Config getDefault() {
        return null;
    }

    default CompoundTag writeSyncingTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ConfigName", getFileName());

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

    String getFileName();

    @Nullable
    default String getModid() {
        return null;
    }

    default boolean hasMenu() {
        return true;
    }
}
