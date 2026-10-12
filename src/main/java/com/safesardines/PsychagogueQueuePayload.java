package com.safesardines;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record PsychagogueQueuePayload(UUID target, int next) implements CustomPayload {
    public static final CustomPayload.Id<PsychagogueQueuePayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "psychagogue_queue"));
    public static final PacketCodec<RegistryByteBuf, PsychagogueQueuePayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeUuid(payload.target());
                buf.writeVarInt(payload.next());
            },
            buf -> new PsychagogueQueuePayload(buf.readUuid(), buf.readVarInt()));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<PsychagogueQueuePayload> {
        @Override
        public void receive(PsychagogueQueuePayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!GameFunctions.isPlayerAliveAndSurvival(player) || !PsychagogueRoles.isPsychagogue(player)) {
                return;
            }
            if (payload.next() < -1 || payload.next() >= PlayerMoodComponent.Task.values().length) {
                return;
            }
            ServerPlayerEntity target = player.getServer().getPlayerManager().getPlayer(payload.target());
            if (target == null || !GameFunctions.isPlayerAliveAndSurvival(target)) {
                return;
            }
            PsychagogueQueue.setNext(target.getUuid(), payload.next() < 0 ? null : PlayerMoodComponent.Task.values()[payload.next()]);
        }
    }
}
