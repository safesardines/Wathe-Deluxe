package com.safesardines.mixin;

import cat.rezelyn.watheextended.component.WatheExtendedWorldComponent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.safesardines.WathedeluxeConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WatheExtendedWorldComponent.class)
public class DeluxeWorldComponentMixin {
    @ModifyReturnValue(method = "isBlockInteractionsProtected", at = @At("RETURN"))
    private boolean wathedeluxe$bypassProtection(boolean original) {
        WathedeluxeConfig config = WathedeluxeConfig.HANDLER.instance();
        return original && !(config.breakAnything || config.placeAnything);
    }
}
