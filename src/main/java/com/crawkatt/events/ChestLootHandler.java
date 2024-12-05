package com.crawkatt.events;

import com.crawkatt.config.ConfigLoader;
import com.crawkatt.util.ChestBlockEntityAccess;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ChestLootHandler {
    public static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!player.isCreative()) return ActionResult.PASS;
            if (player.isSpectator()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            if (world.getBlockState(pos).getBlock() == Blocks.CHEST) {
                ChestBlockEntity chest = (ChestBlockEntity) world.getBlockEntity(pos);
                checkChest(chest, world, pos);
            }

            return ActionResult.PASS;
        });
    }

    private static void checkChest(ChestBlockEntity chest, World world, BlockPos pos) {
        if (chest instanceof ChestBlockEntityAccess accessor && accessor.isUsed()) return;
        if (chest instanceof ChestBlockEntityAccess accesor) accesor.setUsed(true);

        Identifier lootTable = getLootTableForBiome(world, pos);

        if (lootTable != null) chest.setLootTable(lootTable, world.getRandom().nextLong());
        chest.markDirty();
    }

    private static Identifier getLootTableForBiome(World world, BlockPos pos) {
        String biomeId = world.getRegistryManager()
                .get(RegistryKeys.BIOME)
                .getEntry(world.getBiome(pos).getKey().orElseThrow())
                .orElseThrow()
                .getKey()
                .map(key -> key.getValue().toString())
                .orElseThrow();

        List<Identifier> lootTables = ConfigLoader.getLootTables(biomeId);
        if (!lootTables.isEmpty()) return lootTables.get(new Random().nextInt(lootTables.size()));

        return ConfigLoader.defaultLoot.get(new Random().nextInt(ConfigLoader.defaultLoot.size()));
    }
}
