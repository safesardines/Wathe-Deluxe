package com.safesardines.client.mixin;

import com.safesardines.client.ForcedTask;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.doctor4t.wathe.client.gui.MoodRenderer$TaskRenderer")
public class PsychagogueTaskRendererMixin {
    @Shadow
    public Text text;

    @Inject(method = "tick", at = @At("TAIL"))
    private void psychagogue$forceText(PlayerMoodComponent.TrainTask present, float delta, CallbackInfoReturnable<Boolean> cir) {
        if (present == null || !WatheClient.isKiller() || !ForcedTask.matches(present.getType())) {
            return;
        }
        this.text = Text.translatable("psychagogue.asked." + present.getName());
    }
}
