package com.safesardines;

import dev.doctor4t.wathe.api.event.GameEvents;
import com.safesardines.command.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class wathedeluxe implements ModInitializer {
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        WathedeluxeBlocks.register();
        WathedeluxeItems.register();
        RazorRoles.register();
        PsychagogueRoles.register();
        PayloadTypeRegistry.playC2S().register(RazorThrowPayload.ID, RazorThrowPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShivPayload.ID, ShivPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RazorReclaimPayload.ID, RazorReclaimPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PsychagogueRequestPayload.ID, PsychagogueRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PsychagogueQueuePayload.ID, PsychagogueQueuePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PsychagogueDataPayload.ID, PsychagogueDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PsychagogueForcedPayload.ID, PsychagogueForcedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShivCooldownPayload.ID, ShivCooldownPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RazorThrowPayload.ID, new RazorThrowPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(ShivPayload.ID, new ShivPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(RazorReclaimPayload.ID, new RazorReclaimPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(PsychagogueRequestPayload.ID, new PsychagogueRequestPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(PsychagogueQueuePayload.ID, new PsychagogueQueuePayload.Receiver());
        ServerTickEvents.END_SERVER_TICK.register(PsychagogueQueue::tick);
        ServerTickEvents.END_SERVER_TICK.register(ShivCooldowns::tick);
        ServerTickEvents.END_SERVER_TICK.register(server -> VoiceDefer.tick());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MapVariablesCommand.register(dispatcher);
            GameSettingsCommand.register(dispatcher);
            GiveRoomKeyCommand.register(dispatcher);
            StartCommand.register(dispatcher);
            StopCommand.register(dispatcher);
            SetVisualCommand.register(dispatcher);
            ForceRoleCommand.register(dispatcher);
            SetTimerCommand.register(dispatcher);
            SetMoneyCommand.register(dispatcher);
            UpdateDoorsCommand.register(dispatcher);
            PoisonCommand.register(dispatcher);
            BleedingCommand.register(dispatcher);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(started -> server = started);
        ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> server = null);
        GameEvents.ON_GAME_START.register(mode -> onRound());
        GameEvents.ON_GAME_STOP.register(mode -> onRound());
    }

    private static void onRound() {
        PsychagogueQueue.clear();
        ShivCooldowns.clear();
        resetBleeding();
        killKnives();
        if (server == null) {
            return;
        }
        for (ServerWorld world : server.getWorlds()) {
            TemporaryBlocks.sweepWorld(world);
        }
    }

    private static void resetBleeding() {
        if (server == null) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerBleedingComponent.KEY.get(player).reset();
        }
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
