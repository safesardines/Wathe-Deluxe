package com.safesardines;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LibertinariCooldownPayload(int ticks) implements CustomPayload {
    public static final CustomPayload.Id<LibertinariCooldownPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "libertinari_cooldown"));
    public static final PacketCodec<RegistryByteBuf, LibertinariCooldownPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeVarInt(payload.ticks()),
            buf -> new LibertinariCooldownPayload(buf.readVarInt()));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
