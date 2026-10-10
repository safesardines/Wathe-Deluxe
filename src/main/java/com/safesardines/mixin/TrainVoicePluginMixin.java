package com.safesardines.mixin;

import com.safesardines.VoiceDefer;
import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrainVoicePlugin.class)
public class TrainVoicePluginMixin {
    @Inject(method = "addPlayer", at = @At("HEAD"), cancellable = true)
    private static void wathedeluxe$deferAdd(UUID uuid, CallbackInfo ci) {
        if (VoiceDefer.consume(uuid)) {
            VoiceDefer.schedule(uuid);
            ci.cancel();
        }
    }
}
