package com.safesardines;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record LibertinariArmedPayload(List<Integer> ids) implements CustomPayload {
    public static final CustomPayload.Id<LibertinariArmedPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "libertinari_armed"));
    public static final PacketCodec<RegistryByteBuf, LibertinariArmedPayload> CODEC = PacketCodec.of(LibertinariArmedPayload::write, LibertinariArmedPayload::read);

    private static void write(LibertinariArmedPayload payload, RegistryByteBuf buf) {
        buf.writeVarInt(payload.ids().size());
        for (int id : payload.ids()) {
            buf.writeVarInt(id);
        }
    }

    private static LibertinariArmedPayload read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        List<Integer> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(buf.readVarInt());
        }
        return new LibertinariArmedPayload(ids);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
