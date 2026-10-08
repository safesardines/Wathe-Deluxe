package com.safesardines;

import dev.doctor4t.wathe.api.event.GameEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class wathedeluxe implements ModInitializer {
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        RazorRoles.register();
        PayloadTypeRegistry.playC2S().register(RazorThrowPayload.ID, RazorThrowPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RazorStabPayload.ID, RazorStabPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RazorReclaimPayload.ID, RazorReclaimPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RazorThrowPayload.ID, new RazorThrowPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(RazorStabPayload.ID, new RazorStabPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(RazorReclaimPayload.ID, new RazorReclaimPayload.Receiver());
        ServerLifecycleEvents.SERVER_STARTED.register(started -> server = started);
        ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> server = null);
        GameEvents.ON_GAME_START.register(mode -> killKnives());
        GameEvents.ON_GAME_STOP.register(mode -> killKnives());
    }

    private static void killKnives() {
        if (server == null) {
            return;
        }
        for (ServerWorld world : server.getWorlds()) {
            List<ThrownKnifeEntity> knives = new ArrayList<>();
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof ThrownKnifeEntity knife) {
                    knives.add(knife);
                }
            }
            knives.forEach(Entity::discard);
        }
    }
}
