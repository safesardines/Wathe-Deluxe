package com.safesardines;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShivCooldownPayload(int ticks) implements CustomPayload {
    public static final CustomPayload.Id<ShivCooldownPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "shiv_cooldown"));
    public static final PacketCodec<RegistryByteBuf, ShivCooldownPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeVarInt(payload.ticks()),
            buf -> new ShivCooldownPayload(buf.readVarInt()));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
