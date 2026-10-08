package com.crawkatt.events;

import com.crawkatt.ChestLootMod;
import com.crawkatt.config.ConfigLoader;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ChestLootHandler {
    private static final Random RANDOM = new Random();
    public static final Identifier USED_COMPONENT_ID = Identifier.of(ChestLootMod.MOD_ID, "used");
    public static final ComponentType<Boolean> USED_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).build();
    private static final ComponentType<Boolean> SURVIVAL_PLACED_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).build();
    private static final ComponentType<Long> REFILL_VERSION_COMPONENT = ComponentType.<Long>builder().codec(Codec.LONG).build();

    public static void registerEvents() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, USED_COMPONENT_ID, USED_COMPONENT);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(ChestLootMod.MOD_ID, "survival_placed"), SURVIVAL_PLACED_COMPONENT);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(ChestLootMod.MOD_ID, "refill_version"), REFILL_VERSION_COMPONENT);

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> resetChunk(chunk, ChestLootState.get(world.getServer()).refillVersion));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || player.isSpectator()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            if (isChest(world, pos)) handleChest(world, pos);
            return ActionResult.PASS;
        });
    }

    public static void markSurvivalPlaced(ChestBlockEntity chest) {
        chest.setComponents(ComponentMap.builder()
                .addAll(chest.getComponents())
                .add(USED_COMPONENT, true)
                .add(SURVIVAL_PLACED_COMPONENT, true)
                .build()
        );

        chest.markDirty();
    }

    public static int refill(MinecraftServer server) {
        ChestLootState state = ChestLootState.get(server);
        state.refill();

        int count = 0;
        for (ServerWorld world : server.getWorlds()) {
            for (ChunkHolder holder : world.getChunkManager().chunkLoadingManager.entryIterator()) {
                WorldChunk chunk = holder.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).orElse(null);
                if (chunk != null) count += resetChunk(chunk, state.refillVersion);
            }
        }
        return count;
    }

    private static int resetChunk(WorldChunk chunk, long refillVersion) {
        int count = 0;
        for (BlockPos pos : chunk.getBlockEntityPositions()) {
            if (chunk.getBlockEntity(pos) instanceof ChestBlockEntity chest && resetChest(chest, refillVersion)) count++;
        }

        return count;
    }

    private static boolean resetChest(ChestBlockEntity chest, long refillVersion) {
        if (chest.getComponents().getOrDefault(SURVIVAL_PLACED_COMPONENT, false)) return false;
        if (!chest.getComponents().getOrDefault(USED_COMPONENT, false)) return false;
        if (chest.getComponents().getOrDefault(REFILL_VERSION_COMPONENT, 0L) == refillVersion) return false;

        chest.setComponents(ComponentMap.builder()
                .addAll(chest.getComponents())
                .add(USED_COMPONENT, null)
                .add(REFILL_VERSION_COMPONENT, refillVersion)
                .build()
        );

        chest.markDirty();
        return true;
    }

    private static boolean isChest(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.CHEST;
    }

    private static void handleChest(World world, BlockPos pos) {
        Optional<ChestBlockEntity> chest = getChestBlockEntity(world, pos);
        chest.ifPresent(ch -> processChest(ch, world, pos));
    }

    private static Optional<ChestBlockEntity> getChestBlockEntity(World world, BlockPos pos) {
        return Optional.ofNullable(world.getBlockEntity(pos))
                .filter(blockEntity -> blockEntity instanceof ChestBlockEntity)
                .map(blockEntity -> (ChestBlockEntity) blockEntity);
    }

    private static void processChest(ChestBlockEntity chest, World world, BlockPos pos) {
        long refillVersion = ChestLootState.get(world.getServer()).refillVersion;
        resetChest(chest, refillVersion);
        if (chest.getComponents().getOrDefault(ChestLootHandler.USED_COMPONENT, false)) return;

        chest.setComponents(ComponentMap.builder()
                .addAll(chest.getComponents())
                .add(ChestLootHandler.USED_COMPONENT, true)
                .add(REFILL_VERSION_COMPONENT, refillVersion)
                .build()
        );

        assignLootTable(chest, world, pos);
        chest.markDirty();
    }

    private static void assignLootTable(ChestBlockEntity chest, World world, BlockPos pos) {
        Optional.ofNullable(getLootTableForBiome(world, pos))
                .ifPresent(lootTable -> chest.setLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTable), RANDOM.nextLong()));
    }

    private static Identifier getLootTableForBiome(World world, BlockPos pos) {
        return getBiomeIdentifier(world, pos)
                .flatMap(biomeId -> getRandomLootTable(ConfigLoader.getLootTables(biomeId)))
                .orElseGet(() -> getRandomLootTable(ConfigLoader.defaultLoot).orElse(null));
    }

    private static Optional<String> getBiomeIdentifier(World world, BlockPos pos) {
        return world.getRegistryManager()
                .get(RegistryKeys.BIOME)
                .getEntry(world.getBiome(pos).getKey().orElseThrow())
                .flatMap(RegistryEntry.Reference::getKey)
                .map(key -> key.getValue().toString());
    }

    private static Optional<Identifier> getRandomLootTable(List<Identifier> lootTables) {
        if (lootTables.isEmpty()) return Optional.empty();
        return Optional.of(lootTables.get(RANDOM.nextInt(lootTables.size())));
    }
}
