package com.safesardines;

import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record LibertinariDetonatePayload() implements CustomPayload {
    public static final CustomPayload.Id<LibertinariDetonatePayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "libertinari_detonate"));
    public static final PacketCodec<RegistryByteBuf, LibertinariDetonatePayload> CODEC = PacketCodec.unit(new LibertinariDetonatePayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<LibertinariDetonatePayload> {
        @Override
        public void receive(LibertinariDetonatePayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!GameFunctions.isPlayerAliveAndSurvival(player) || !LibertinariRoles.isLibertinari(player)) {
                return;
            }
            if (!LibertinariCooldowns.ready(player)) {
                return;
            }
            if (LibertinariCorpses.detonate(player)) {
                LibertinariCorpses.sync(player.getServer());
                LibertinariCooldowns.trigger(player, LibertinariCooldowns.DETONATE_COOLDOWN);
            }
        }
    }
}
