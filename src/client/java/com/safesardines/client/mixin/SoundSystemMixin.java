package com.safesardines.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.safesardines.client.BleedSound;
import com.safesardines.client.HearingLoss;
import net.minecraft.client.sound.SoundInstance;
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

    @ModifyReturnValue(method = "getAdjustedVolume(Lnet/minecraft/client/sound/SoundInstance;)F", at = @At("RETURN"))
    private float wathedeluxe$loudHearingLoss(float original, SoundInstance sound) {
        if (sound instanceof HearingLoss) {
            return original * HearingLoss.BOOST;
        }
        return original;
    }
}
