package com.crawkatt.mixin;

import com.crawkatt.events.ChestLootHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class ChestPlacementMixin {
    @Inject(method = "onPlaced", at = @At("TAIL"))
    private void markSurvivalChest(
            World world,
            BlockPos pos,
            BlockState state,
            LivingEntity placer,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (state.isOf(Blocks.CHEST)
                && placer instanceof ServerPlayerEntity player
                && player.interactionManager.getGameMode() == GameMode.SURVIVAL
                && world.getBlockEntity(pos) instanceof ChestBlockEntity chest
        ) {
            ChestLootHandler.markSurvivalPlaced(chest);
        }
    }
}