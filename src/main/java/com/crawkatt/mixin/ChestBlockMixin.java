package com.crawkatt.mixin;

import com.crawkatt.util.ChestBlockEntityAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {

    @Inject(method = "onPlaced", at = @At("TAIL"))
    private void onChestPlaced(World world, BlockPos pos, BlockState state, PlayerEntity placer, ItemStack itemStack, CallbackInfo ci) {
        if (shouldProcess(world, placer)) processChest(world, pos, state);
    }

    private boolean shouldProcess(World world, PlayerEntity placer) {
        return !world.isClient && placer != null && !placer.isCreative();
    }

    private void processChest(World world, BlockPos pos, BlockState state) {
        getChestBlockEntity(world, pos).ifPresent(chest -> {
            if (chest instanceof ChestBlockEntityAccess accessor) {
                accessor.setUsed(true);
                chest.markDirty();
                world.updateListeners(pos, state, state, 3);
            }
        });
    }

    private Optional<ChestBlockEntity> getChestBlockEntity(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof ChestBlockEntity chest ? Optional.of(chest) : Optional.empty();
    }
}
