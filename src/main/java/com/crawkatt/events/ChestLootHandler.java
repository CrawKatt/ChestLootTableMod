package com.crawkatt.events;

import com.crawkatt.config.ConfigLoader;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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
                    NbtCompound nbt = chest.getStack(0).getOrCreateSubNbt("used");
                    if (nbt.getBoolean("used")) {
                        System.out.println("El cofre ya ha sido usado");
                        return ActionResult.PASS;
                    }

                    RegistryEntry<?> biomeEntry = world.getRegistryManager()
                            .get(RegistryKeys.BIOME)
                            .getEntry(world.getBiome(pos).getKey().orElseThrow())
                            .orElseThrow();

                    Optional<? extends RegistryKey<?>> biomeKey = biomeEntry.getKey();
                    if (biomeKey.isPresent()) {
                        String biomeId = biomeKey.get().getValue().toString();
                        List<Identifier> lootTables = ConfigLoader.getLootTables(biomeId);

                        if (!lootTables.isEmpty()) {
                            Identifier lootTable = lootTables.get(new Random().nextInt(lootTables.size()));
                            chest.setLootTable(lootTable, new Random().nextLong());
                        }

                        nbt.putBoolean("used", true);
                        System.out.println("El cofra ha sido usado");
                        chest.readNbt(nbt);
                        chest.markDirty();
                    }
                }
                return ActionResult.PASS;
            }
            return ActionResult.PASS;
        });
    }
}
