package com.safesardines.client;

import com.safesardines.RazorRoles;
import com.safesardines.ShivPayload;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public final class ShivController {
    private static final int HOLD_TICKS = 10;
    private static int holdTimer = -1;
    private static int originalSlot;
    private static int cooldownRemaining;

    private ShivController() {
    }

    public static void shiv(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || !isRazorAlive(player)) {
            return;
        }
        int slot = findKnife(player);
        if (slot < 0 || player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)) {
            return;
        }
        PlayerEntity target = ShivPayload.findTarget(player);
        if (target == null) {
            return;
        }
        if (holdTimer < 0) {
            originalSlot = player.getInventory().selectedSlot;
        }
        if (player.getInventory().selectedSlot != slot) {
            select(client, player, slot);
        }
        holdTimer = HOLD_TICKS;
        player.swingHand(Hand.MAIN_HAND);
        ClientPlayNetworking.send(new ShivPayload());
    }

    public static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || !player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)) {
            cooldownRemaining = 0;
        } else if (cooldownRemaining > 0) {
            cooldownRemaining--;
        }
        if (holdTimer < 0) {
            return;
        }
        holdTimer--;
        if (holdTimer < 0 && player != null) {
            select(client, player, originalSlot);
        }
    }

    public static boolean isShivving() {
        return holdTimer >= 0;
    }

    public static void setCooldown(int ticks) {
        cooldownRemaining = ticks;
    }

    public static boolean hasSeconds() {
        return cooldownRemaining > 0;
    }

    public static int cooldownSeconds() {
        return (cooldownRemaining + 19) / 20;
    }

    private static void select(MinecraftClient client, PlayerEntity player, int slot) {
        player.getInventory().selectedSlot = slot;
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler != null) {
            handler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }

    private static int findKnife(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(WatheItems.KNIFE)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isRazorAlive(PlayerEntity player) {
        return GameFunctions.isPlayerAliveAndSurvival(player) && GameWorldComponent.KEY.get(player.getWorld()).isRole(player, RazorRoles.RAZOR);
    }
}
