package com.safesardines.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.safesardines.client.ShivController;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerEntity.class, priority = 2000)
public class ShivSlowMixin {
    @ModifyReturnValue(method = "getMovementSpeed", at = @At("RETURN"))
    private float wathedeluxe$shivSlow(float original) {
        if (ShivController.isShivving()) {
            return original * 0.6F;
        }
        return original;
    }
}
