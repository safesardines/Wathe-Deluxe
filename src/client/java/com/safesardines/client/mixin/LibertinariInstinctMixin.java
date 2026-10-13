package com.safesardines.client.mixin;

import com.safesardines.LibertinariRoles;
import com.safesardines.client.LibertinariController;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WatheClient.class)
public abstract class LibertinariInstinctMixin {
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void libertinari$corpseGlow(Entity target, CallbackInfoReturnable<Integer> cir) {
        if (!(target instanceof PlayerBodyEntity)) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        if (LibertinariRoles.isLibertinari(client.player) && LibertinariController.isArmed(target.getId())) {
            cir.setReturnValue(0xFF0000);
        }
    }
}
