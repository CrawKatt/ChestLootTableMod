package com.crawkatt.mixin;

import com.crawkatt.events.ChestLootHandler;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin {
	@Inject(method = "readNbt", at = @At("TAIL"))
	private void migrateUsedNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		if (!nbt.contains("used")) return;

		NbtCompound components = nbt.getCompound("components");
		String componentId = ChestLootHandler.USED_COMPONENT_ID.toString();
		if (!components.contains(componentId)) {
			components.putBoolean(componentId, nbt.getBoolean("used"));
			nbt.put("components", components);
		}
		nbt.remove("used");
	}
}