package com.safesardines.client.mixin;

import com.safesardines.client.ForcedTask;
import dev.doctor4t.wathe.client.gui.MoodRenderer;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MoodRenderer.class)
public class PsychagogueMoodRendererMixin {
    @Redirect(method = "renderKiller", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;hsvToRgb(FFF)I"))
    private static int psychagogue$purpleBar(float hue, float saturation, float value) {
        if (ForcedTask.active()) {
            return 0x8A2BE2;
        }
        return MathHelper.hsvToRgb(hue, saturation, value);
    }
}
