package com.crawkatt.events;

import com.crawkatt.ChestLootMod;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

class ChestLootState extends PersistentState {
    private static final Type<ChestLootState> TYPE = new Type<>(
            () -> new ChestLootState(0),
            (nbt, registryLookup) -> new ChestLootState(nbt.getLong("refill_version")),
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public long refillVersion;

    private ChestLootState(long refillVersion) {
        this.refillVersion = refillVersion;
    }

    static ChestLootState get(MinecraftServer server) {
        return server.getOverworld()
                .getPersistentStateManager()
                .getOrCreate(TYPE, ChestLootMod.MOD_ID);
    }

    void refill() {
        refillVersion++;
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLong("refill_version", refillVersion);
        return nbt;
    }
}