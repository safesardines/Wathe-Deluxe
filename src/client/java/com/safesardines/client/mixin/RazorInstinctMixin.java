package com.safesardines.client.mixin;

import com.safesardines.RazorRoles;
import com.safesardines.ThrownKnifeEntity;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WatheClient.class)
public abstract class RazorInstinctMixin {
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void wathedeluxe$razorKnifeGlow(Entity target, CallbackInfoReturnable<Integer> cir) {
        boolean knife = target instanceof ThrownKnifeEntity
                || (target instanceof ItemEntity item && item.getStack().isOf(WatheItems.KNIFE));
        if (!knife) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        if (WatheClient.isInstinctEnabled()
                && GameWorldComponent.KEY.get(client.world).isRole(client.player, RazorRoles.RAZOR)) {
            cir.setReturnValue(0xDB9D00);
        } else {
            cir.setReturnValue(-1);
        }
    }
}
