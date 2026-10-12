package com.safesardines.client;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.client.MinecraftClient;

public final class ForcedTask {
    private static int task = -1;
    private static boolean seen;

    private ForcedTask() {
    }

    public static void set(int ordinal) {
        task = ordinal;
        seen = false;
    }

    public static void tick(MinecraftClient client) {
        if (task < 0) {
            return;
        }
        if (client.player == null) {
            task = -1;
            seen = false;
            return;
        }
        if (active()) {
            seen = true;
        } else if (seen) {
            task = -1;
            seen = false;
        }
    }

    public static boolean active() {
        if (task < 0) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        return PlayerMoodComponent.KEY.get(client.player).tasks.containsKey(PlayerMoodComponent.Task.values()[task]);
    }

    public static boolean matches(PlayerMoodComponent.Task type) {
        return task >= 0 && type.ordinal() == task;
    }
}
