package com.safesardines.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.safesardines.WathedeluxeConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Entity.class, priority = 2000)
public class DeluxeCollisionMixin {
    @WrapMethod(method = "collidesWith")
    private boolean wathedeluxe$walkingCollision(Entity other, Operation<Boolean> original) {
        Entity self = (Entity) (Object) this;
        if (self instanceof PlayerEntity selfPlayer && other instanceof PlayerEntity otherPlayer) {
            if (WathedeluxeConfig.HANDLER.instance().collisionsWhileWalking) {
                return !selfPlayer.isSprinting() && !otherPlayer.isSprinting();
            }
        }
        return original.call(other);
    }
}
