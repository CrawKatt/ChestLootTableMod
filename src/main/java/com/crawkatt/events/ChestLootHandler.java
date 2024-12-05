package com.crawkatt.events;

import com.crawkatt.config.ConfigLoader;
import com.crawkatt.util.ChestBlockEntityAccess;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ChestLootHandler {
    private static final Random RANDOM = new Random();

    public static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient || !player.isCreative() || player.isSpectator()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            if (isChest(world, pos)) handleChest(world, pos);
            return ActionResult.PASS;
        });
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
        if (chest instanceof ChestBlockEntityAccess accessor) {
            if (accessor.isUsed()) return;

            accessor.setUsed(true);
            assignLootTable(chest, world, pos);
            chest.markDirty();
        }
    }

    private static void assignLootTable(ChestBlockEntity chest, World world, BlockPos pos) {
        Optional.ofNullable(getLootTableForBiome(world, pos))
                .ifPresent(lootTable -> chest.setLootTable(lootTable, RANDOM.nextLong()));
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
