package com.safesardines.client;

import com.safesardines.VoiceMuffle;
import net.minecraft.client.MinecraftClient;

public final class BleedSound {
    private static HearingLoss sound;

    private BleedSound() {
    }

    public static void tick(MinecraftClient client) {
        VoiceMuffle.setHearingLoss(hearingLoss());
        if (BleedOverlay.intensity() > 0.01F) {
            if (sound == null || !client.getSoundManager().isPlaying(sound)) {
                sound = new HearingLoss();
                client.getSoundManager().play(sound);
            }
        } else if (sound != null) {
            sound.finish();
            sound = null;
        }
    }

    public static float hearingLoss() {
        return BleedOverlay.intensity() * 0.9F;
    }
}
