package com.crawkatt.events;

import com.crawkatt.ChestLootMod;
import com.crawkatt.config.ConfigLoader;
import com.crawkatt.util.ChestBlockEntityAccess;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
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
    public static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            if (world.getBlockState(pos).getBlock() == Blocks.CHEST) {
                ChestBlockEntity chest = (ChestBlockEntity) world.getBlockEntity(pos);
                if (chest != null) {
                    if (chest instanceof ChestBlockEntityAccess accessor && accessor.isUsed()) {
                        ChestLootMod.LOGGER.info("El cofre ya fue usado. No se generará loot.");
                        return ActionResult.PASS;
                    }

                    if (chest instanceof ChestBlockEntityAccess accesor) {
                        accesor.setUsed(true);
                        ChestLootMod.LOGGER.info("Generando loot y marcando el cofre como usado.");
                    }

                    Identifier lootTable = getLootTableForBiome(world, pos);
                    if (lootTable != null) {
                        chest.setLootTable(lootTable, world.getRandom().nextLong());
                    }
                    chest.markDirty();
                }
            }

            return ActionResult.SUCCESS;
        });
    }

    private static Identifier getLootTableForBiome(World world, BlockPos pos) {
        RegistryEntry<?> biomeEntry = world.getRegistryManager()
                .get(RegistryKeys.BIOME)
                .getEntry(world.getBiome(pos).getKey().orElseThrow())
                .orElseThrow();

        Optional<? extends RegistryKey<?>> biomeKey = biomeEntry.getKey();
        if (biomeKey.isPresent()) {
            String biomeId = biomeKey.get().getValue().toString();
            List<Identifier> lootTables = ConfigLoader.getLootTables(biomeId);

            if (!lootTables.isEmpty()) {
                return lootTables.get(new Random().nextInt(lootTables.size()));
            }

            if (!ConfigLoader.defaultLoot.isEmpty()) {
                return ConfigLoader.defaultLoot.get(new Random().nextInt(ConfigLoader.defaultLoot.size()));
            }
        }

        return null;
    }
}
