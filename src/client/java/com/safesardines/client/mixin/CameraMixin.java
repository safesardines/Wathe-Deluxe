package com.safesardines.client.mixin;

import com.safesardines.client.BleedingShake;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {
    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, index = 1)
    private float wathedeluxe$shakeYaw(float yaw) {
        return yaw + BleedingShake.yawOffset();
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, index = 2)
    private float wathedeluxe$shakePitch(float pitch) {
        return pitch + BleedingShake.pitchOffset();
    }
}
