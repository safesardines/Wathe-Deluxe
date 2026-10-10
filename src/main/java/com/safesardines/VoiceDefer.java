package com.safesardines;

import dev.doctor4t.wathe.compat.TrainVoicePlugin;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class VoiceDefer {
    private static final int DELAY = 110;
    private static final Set<UUID> deferred = new HashSet<>();
    private static final Map<UUID, Integer> pending = new HashMap<>();

    private VoiceDefer() {
    }

    public static void defer(UUID uuid) {
        deferred.add(uuid);
    }

    public static boolean consume(UUID uuid) {
        return deferred.remove(uuid);
    }

    public static void schedule(UUID uuid) {
        pending.put(uuid, DELAY);
    }

    public static void tick() {
        if (pending.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, Integer>> iterator = pending.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int ticks = entry.getValue() - 1;
            if (ticks <= 0) {
                TrainVoicePlugin.addPlayer(entry.getKey());
                iterator.remove();
            } else {
                entry.setValue(ticks);
            }
        }
    }
}
