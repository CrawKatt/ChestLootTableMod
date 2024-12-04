package com.crawkatt.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.server.world.ServerWorld;

public class BlockPlaceHandler {
    public static void registerEvents() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(((blockEntity, world) -> {
            if (blockEntity instanceof ChestBlockEntity chest) {
                if (world instanceof ServerWorld) {
                    if (!world.isClient) {
                        if (chest.createNbt().contains("used")) {
                            return;
                        }

                        chest.createNbt().putBoolean("used", true);
                    }
                }
            }
        }));
    }
}
