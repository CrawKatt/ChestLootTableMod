package com.crawkatt.mixin;

import com.crawkatt.util.ChestBlockEntityAccess;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin implements ChestBlockEntityAccess {
	@Unique
	private boolean used = false; // Nuestra variable para el NBT personalizado

	// Leer el NBT personalizado
	@Inject(method = "readNbt", at = @At("TAIL"))
	private void readUsedNbt(NbtCompound nbt, CallbackInfo ci) {
		this.used = nbt.getBoolean("used");
	}

	// Escribir el NBT personalizado
	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void writeUsedNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putBoolean("used", this.used);
	}

	@Override
	public boolean isUsed() {
		return this.used;
	}

	@Override
	public void setUsed(boolean used) {
		this.used = used;
	}
}