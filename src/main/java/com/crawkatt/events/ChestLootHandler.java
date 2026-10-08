package com.crawkatt.events;

import com.crawkatt.ChestLootMod;
import com.crawkatt.config.ConfigLoader;
import com.mojang.serialization.Codec;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ChestLootHandler {
    private static final Random RANDOM = new Random();
    public static final Identifier USED_COMPONENT_ID = Identifier.of(ChestLootMod.MOD_ID, "used");
    public static final ComponentType<Boolean> USED_COMPONENT = ComponentType.<Boolean>builder().codec(Codec.BOOL).build();

    public static void registerEvents() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, USED_COMPONENT_ID, USED_COMPONENT);
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
        if (chest.getComponents().getOrDefault(ChestLootHandler.USED_COMPONENT, false)) return;

        chest.setComponents(ComponentMap.builder()
                .addAll(chest.getComponents())
                .add(ChestLootHandler.USED_COMPONENT, true)
                .build());
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
