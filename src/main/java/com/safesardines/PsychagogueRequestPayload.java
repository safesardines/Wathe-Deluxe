package com.safesardines;

import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PsychagogueRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<PsychagogueRequestPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "psychagogue_request"));
    public static final PacketCodec<RegistryByteBuf, PsychagogueRequestPayload> CODEC = PacketCodec.unit(new PsychagogueRequestPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<PsychagogueRequestPayload> {
        @Override
        public void receive(PsychagogueRequestPayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!GameFunctions.isPlayerAliveAndSurvival(player) || !PsychagogueRoles.isPsychagogue(player)) {
                return;
            }
            if (!(player.getWorld() instanceof ServerWorld world)) {
                return;
            }
            GameWorldComponent game = GameWorldComponent.KEY.get(world);
            List<PsychagogueDataPayload.Entry> entries = new ArrayList<>();
            for (ServerPlayerEntity target : world.getPlayers()) {
                if (!GameFunctions.isPlayerAliveAndSurvival(target)) {
                    continue;
                }
                PlayerMoodComponent component = PlayerMoodComponent.KEY.get(target);
                Role role = game.getRole(target);
                int task = -1;
                for (PlayerMoodComponent.TrainTask trainTask : component.tasks.values()) {
                    task = trainTask.getType().ordinal();
                    break;
                }
                PlayerMoodComponent.Task next = PsychagogueQueue.next(target.getUuid());
                entries.add(new PsychagogueDataPayload.Entry(target.getUuid(), task, next == null ? -1 : next.ordinal(), role == null ? Role.MoodType.NONE.ordinal() : role.getMoodType().ordinal(), component.getMood()));
            }
            ServerPlayNetworking.send(player, new PsychagogueDataPayload(entries));
        }
    }
}
