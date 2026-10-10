package com.safesardines.client;

public final class BleedingShake {
    private static final float MAX_ANGLE = 0.6F;

    private BleedingShake() {
    }

    public static float yawOffset() {
        return jitter(0.0F) * amplitude();
    }

    public static float pitchOffset() {
        return jitter(1.7F) * amplitude();
    }

    private static float amplitude() {
        return BleedOverlay.intensity() * MAX_ANGLE;
    }

    private static float jitter(float seed) {
        double t = System.nanoTime() / 1.0E9;
        return (float) ((Math.sin(t * 31.0 + seed) + Math.sin(t * 47.0 + seed * 3.1)) * 0.5);
    }
}
