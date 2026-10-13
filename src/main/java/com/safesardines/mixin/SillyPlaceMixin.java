package com.safesardines.mixin;

import com.safesardines.TemporaryBlocks;
import com.safesardines.WathedeluxeConfig;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class SillyPlaceMixin {
    @Inject(method = "place", at = @At("HEAD"))
    private void wathedeluxe$trackPlacement(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!WathedeluxeConfig.HANDLER.instance().placeAnything) {
            return;
        }
        if (context.getWorld() instanceof ServerWorld world) {
            TemporaryBlocks.temporaryPlace(world, context.getBlockPos());
        }
    }
}
