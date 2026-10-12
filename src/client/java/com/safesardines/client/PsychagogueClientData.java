package com.safesardines.client;

import com.safesardines.PsychagogueDataPayload;

import java.util.List;
import java.util.UUID;

public final class PsychagogueClientData {
    private static List<PsychagogueDataPayload.Entry> entries = List.of();

    private PsychagogueClientData() {
    }

    public static void set(List<PsychagogueDataPayload.Entry> newEntries) {
        entries = newEntries;
    }

    public static PsychagogueDataPayload.Entry get(UUID uuid) {
        for (PsychagogueDataPayload.Entry entry : entries) {
            if (entry.uuid().equals(uuid)) {
                return entry;
            }
        }
        return null;
    }

    public static int next(UUID uuid) {
        PsychagogueDataPayload.Entry entry = get(uuid);
        return entry == null ? -1 : entry.next();
    }

    public static void setNext(UUID uuid, int next) {
        for (int i = 0; i < entries.size(); i++) {
            PsychagogueDataPayload.Entry entry = entries.get(i);
            if (entry.uuid().equals(uuid)) {
                entries.set(i, new PsychagogueDataPayload.Entry(entry.uuid(), entry.task(), next, entry.moodType(), entry.mood()));
                return;
            }
        }
    }
}
