package com.safesardines;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PsychagogueDataPayload(List<Entry> entries) implements CustomPayload {
    public static final CustomPayload.Id<PsychagogueDataPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "psychagogue_data"));
    public static final PacketCodec<RegistryByteBuf, PsychagogueDataPayload> CODEC = PacketCodec.of(PsychagogueDataPayload::write, PsychagogueDataPayload::read);

    public record Entry(UUID uuid, int task, int next, int moodType, float mood) {
    }

    private static void write(PsychagogueDataPayload payload, RegistryByteBuf buf) {
        buf.writeVarInt(payload.entries().size());
        for (Entry entry : payload.entries()) {
            buf.writeUuid(entry.uuid());
            buf.writeVarInt(entry.task());
            buf.writeVarInt(entry.next());
            buf.writeVarInt(entry.moodType());
            buf.writeFloat(entry.mood());
        }
    }

    private static PsychagogueDataPayload read(RegistryByteBuf buf) {
        int size = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUuid();
            int task = buf.readVarInt();
            int next = buf.readVarInt();
            int moodType = buf.readVarInt();
            float mood = buf.readFloat();
            entries.add(new Entry(uuid, task, next, moodType, mood));
        }
        return new PsychagogueDataPayload(entries);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
