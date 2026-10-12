package com.safesardines;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PsychagogueForcedPayload(int task) implements CustomPayload {
    public static final CustomPayload.Id<PsychagogueForcedPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "psychagogue_forced"));
    public static final PacketCodec<RegistryByteBuf, PsychagogueForcedPayload> CODEC = PacketCodec.of(
            (payload, buf) -> buf.writeVarInt(payload.task()),
            buf -> new PsychagogueForcedPayload(buf.readVarInt()));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
