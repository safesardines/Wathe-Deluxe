package com.safesardines;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PsychagogueQueue {
    private static final Map<UUID, PlayerMoodComponent.Task> NEXT = new HashMap<>();

    public static void setNext(UUID target, PlayerMoodComponent.Task task) {
        if (task == null) {
            NEXT.remove(target);
        } else {
            NEXT.put(target, task);
        }
    }

    public static PlayerMoodComponent.Task next(UUID target) {
        return NEXT.get(target);
    }

    public static void clear() {
        NEXT.clear();
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerMoodComponent.Task task = NEXT.get(player.getUuid());
            if (task == null) {
                continue;
            }
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                NEXT.remove(player.getUuid());
                continue;
            }
            PlayerMoodComponent component = PlayerMoodComponent.KEY.get(player);
            if (!component.tasks.isEmpty()) {
                continue;
            }
            NEXT.remove(player.getUuid());
            component.tasks.put(task, create(task));
            component.sync();
            ServerPlayNetworking.send(player, new PsychagogueForcedPayload(task.ordinal()));
        }
    }

    public static PlayerMoodComponent.TrainTask create(PlayerMoodComponent.Task task) {
        return switch (task) {
            case SLEEP -> new PlayerMoodComponent.SleepTask(GameConstants.SLEEP_TASK_DURATION);
            case OUTSIDE -> new PlayerMoodComponent.OutsideTask(GameConstants.OUTSIDE_TASK_DURATION);
            case EAT -> new PlayerMoodComponent.EatTask();
            case DRINK -> new PlayerMoodComponent.DrinkTask();
        };
    }
}
