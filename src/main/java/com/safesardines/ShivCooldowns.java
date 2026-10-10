package com.safesardines;

import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ShivCooldowns {
    private static final int GRACE = 60;
    private static final int PER_SHIV = 40;
    private static final int CAP = 300;
    private static final Map<UUID, int[]> STATE = new HashMap<>();

    public static void onShiv(UUID attacker, int now) {
        int[] state = STATE.computeIfAbsent(attacker, key -> new int[2]);
        state[0] = now;
        state[1]++;
    }

    public static void tick(MinecraftServer server) {
        int now = server.getTicks();
        Iterator<Map.Entry<UUID, int[]>> iterator = STATE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, int[]> entry = iterator.next();
            int[] state = entry.getValue();
            if (now - state[0] >= GRACE) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                if (player != null) {
                    int ticks = Math.min(state[1] * PER_SHIV, CAP);
                    player.getItemCooldownManager().set(WatheItems.KNIFE, ticks);
                    ServerPlayNetworking.send(player, new ShivCooldownPayload(ticks));
                }
                iterator.remove();
            }
        }
    }

    public static void clear() {
        STATE.clear();
    }
}
