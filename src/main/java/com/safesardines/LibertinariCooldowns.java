package com.safesardines;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LibertinariCooldowns {
    public static final int IMPLANT_COOLDOWN = 80;
    public static final int DETONATE_COOLDOWN = 240;
    private static final Map<UUID, Integer> READY_AT = new HashMap<>();

    public static boolean ready(ServerPlayerEntity player) {
        return player.getServer().getTicks() >= READY_AT.getOrDefault(player.getUuid(), 0);
    }

    public static void trigger(ServerPlayerEntity player, int ticks) {
        READY_AT.put(player.getUuid(), player.getServer().getTicks() + ticks);
        ServerPlayNetworking.send(player, new LibertinariCooldownPayload(ticks));
    }

    public static void clear() {
        READY_AT.clear();
    }
}
