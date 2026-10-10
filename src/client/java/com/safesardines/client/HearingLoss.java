package com.safesardines.client;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class HearingLoss extends AbstractSoundInstance implements TickableSoundInstance {
    private boolean done;

    public HearingLoss() {
        super(SoundEvent.of(Identifier.of("wathedeluxe", "memories")), SoundCategory.MASTER, SoundInstance.createRandom());
        this.repeat = true;
        this.repeatDelay = 0;
        this.relative = true;
        this.attenuationType = SoundInstance.AttenuationType.NONE;
    }

    @Override
    public float getVolume() {
        return BleedOverlay.intensity();
    }

    public void finish() {
        this.done = true;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public void tick() {
    }
}
