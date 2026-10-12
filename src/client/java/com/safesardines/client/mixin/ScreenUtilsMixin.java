package com.safesardines.client.mixin;

import cat.rezelyn.watheextended.client.screen.ScreenUtils;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenUtils.class)
public class ScreenUtilsMixin {
    @Inject(method = "icon", at = @At("HEAD"), cancellable = true)
    private static void psychagogue$icon(String name, CallbackInfoReturnable<Text> cir) {
        if (name.equals("psychagogue")) {
            cir.setReturnValue(Text.literal("\uE325").setStyle(ScreenUtils.ICON_STYLE));
        }
    }

    @Inject(method = "hasIcon", at = @At("HEAD"), cancellable = true)
    private static void psychagogue$hasIcon(String name, CallbackInfoReturnable<Boolean> cir) {
        if (name.equals("psychagogue")) {
            cir.setReturnValue(true);
        }
    }
}
