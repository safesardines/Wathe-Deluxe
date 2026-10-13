package com.safesardines;

import dev.doctor4t.wathe.api.event.GameEvents;
import com.safesardines.command.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

public class wathedeluxe implements ModInitializer {
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        WathedeluxeConfig.HANDLER.load();
        WathedeluxeBlocks.register();
        WathedeluxeItems.register();
        RazorRoles.register();
        PsychagogueRoles.register();
        LibertinariRoles.register();
        PayloadTypeRegistry.playC2S().register(RazorThrowPayload.ID, RazorThrowPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ShivPayload.ID, ShivPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RazorReclaimPayload.ID, RazorReclaimPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PsychagogueRequestPayload.ID, PsychagogueRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PsychagogueQueuePayload.ID, PsychagogueQueuePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(LibertinariStuffPayload.ID, LibertinariStuffPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(LibertinariDetonatePayload.ID, LibertinariDetonatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WathedeluxeConfigPayload.ID, WathedeluxeConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PsychagogueDataPayload.ID, PsychagogueDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PsychagogueForcedPayload.ID, PsychagogueForcedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShivCooldownPayload.ID, ShivCooldownPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LibertinariCooldownPayload.ID, LibertinariCooldownPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LibertinariArmedPayload.ID, LibertinariArmedPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RazorThrowPayload.ID, new RazorThrowPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(ShivPayload.ID, new ShivPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(RazorReclaimPayload.ID, new RazorReclaimPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(PsychagogueRequestPayload.ID, new PsychagogueRequestPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(PsychagogueQueuePayload.ID, new PsychagogueQueuePayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(LibertinariStuffPayload.ID, new LibertinariStuffPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(LibertinariDetonatePayload.ID, new LibertinariDetonatePayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(WathedeluxeConfigPayload.ID, new WathedeluxeConfigPayload.Receiver());
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld) || player.isSpectator()) {
                return true;
            }
            boolean silly = WathedeluxeConfig.HANDLER.instance().breakAnything;
            if (silly) {
                TemporaryBlocks.temporaryBreak(serverWorld, pos, WathedeluxeConfig.HANDLER.instance().itemDrops && !player.isCreative(), false);
                return false;
            }
            boolean admin = player.getMainHandStack().isOf(WathedeluxeItems.BREAKITEM) && WathedeluxeConfig.HANDLER.instance().destruction;
            if (admin) {
                TemporaryBlocks.temporaryBreak(serverWorld, pos, false, true);
                return false;
            }
            if (player.isCreative()) {
                return true;
            }
            return !WathedeluxeConfig.HANDLER.instance().placeAnything;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            boolean survival = WathedeluxeConfig.HANDLER.instance().breakAnything || WathedeluxeConfig.HANDLER.instance().placeAnything;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                GameMode mode = player.interactionManager.getGameMode();
                if (survival && mode == GameMode.ADVENTURE) {
                    player.changeGameMode(GameMode.SURVIVAL);
                } else if (!survival && mode == GameMode.SURVIVAL) {
                    player.changeGameMode(GameMode.ADVENTURE);
                }
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(PsychagogueQueue::tick);
        ServerTickEvents.END_SERVER_TICK.register(LibertinariCorpses::tick);
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
        ServerPlayConnectionEvents.JOIN.register((handler, sender, joinedServer) -> LibertinariCorpses.syncTo(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STARTED.register(started -> server = started);
        ServerLifecycleEvents.SERVER_STOPPING.register(stopping -> onRound());
        ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> server = null);
        GameEvents.ON_GAME_START.register(mode -> onRound());
        GameEvents.ON_GAME_STOP.register(mode -> onRound());
    }

    private static void onRound() {
        PsychagogueQueue.clear();
        LibertinariCorpses.clear();
        LibertinariCooldowns.clear();
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
