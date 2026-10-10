package com.safesardines;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceMuffle implements VoicechatPlugin {
    private static volatile float hearingLoss;
    private static final Map<UUID, float[]> hearingLossStates = new ConcurrentHashMap<>();

    public static void setHearingLoss(float loss) {
        hearingLoss = loss;
    }

    @Override
    public String getPluginId() {
        return "wathedeluxe";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, event -> applyHearingLoss(event));
        registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class, event -> applyHearingLoss(event));
        registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, event -> applyHearingLoss(event));
    }

    private static void applyHearingLoss(ClientReceiveSoundEvent event) {
        float loss = hearingLoss;
        if (loss <= 0.01F) {
            if (!hearingLossStates.isEmpty()) {
                hearingLossStates.clear();
            }
            return;
        }
        short[] audio = event.getRawAudio();
        if (audio == null || audio.length == 0) {
            return;
        }
        float alpha = 1.0F - Math.min(1.0F, loss) * 0.95F;
        float gain = 1.0F - Math.min(1.0F, loss);
        float[] state = hearingLossStates.computeIfAbsent(event.getId(), id -> new float[1]);
        float low = state[0];
        for (int i = 0; i < audio.length; i++) {
            float sample = audio[i] / 32768.0F;
            low += alpha * (sample - low);
            float out = low * gain;
            if (out > 1.0F) {
                out = 1.0F;
            } else if (out < -1.0F) {
                out = -1.0F;
            }
            audio[i] = (short) (out * 32767.0F);
        }
        state[0] = low;
        event.setRawAudio(audio);
    }
}
