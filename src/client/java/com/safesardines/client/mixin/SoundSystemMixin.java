package com.safesardines.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.safesardines.client.BleedSound;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @ModifyReturnValue(method = "getAdjustedVolume(FLnet/minecraft/sound/SoundCategory;)F", at = @At("RETURN"))
    private float wathedeluxe$hearingLoss(float original, float volume, SoundCategory category) {
        if (category == SoundCategory.MASTER) {
            return original;
        }
        return original * (1.0F - BleedSound.hearingLoss());
    }
}
