package com.safesardines.client;

import com.safesardines.LibertinariDetonatePayload;
import com.safesardines.LibertinariRoles;
import com.safesardines.LibertinariStuffPayload;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LibertinariController {
    private static final double RANGE = 3.0;
    private static final float MARGIN = 0.3F;
    private static final int HOLD_TICKS = 10;
    private static final Set<Integer> ARMED = new HashSet<>();
    private static int cooldownRemaining;
    private static int holdTimer = -1;
    private static int originalSlot;

    private LibertinariController() {
    }

    public static void ability(MinecraftClient client) {
        PlayerEntity player = client.player;
        if (player == null || !GameFunctions.isPlayerAliveAndSurvival(player) || !LibertinariRoles.isLibertinari(player)) {
            return;
        }
        if (cooldownRemaining > 0) {
            return;
        }
        if (findImplantableCorpse(player) != null) {
            int slot = findGrenade(player);
            if (slot >= 0) {
                if (holdTimer < 0) {
                    originalSlot = player.getInventory().selectedSlot;
                }
                if (player.getInventory().selectedSlot != slot) {
                    select(client, player, slot);
                }
                holdTimer = HOLD_TICKS;
                player.swingHand(Hand.MAIN_HAND);
            }
            ClientPlayNetworking.send(new LibertinariStuffPayload());
        } else {
            ClientPlayNetworking.send(new LibertinariDetonatePayload());
        }
    }

    public static void tick(MinecraftClient client) {
        if (cooldownRemaining > 0) {
            cooldownRemaining--;
        }
        PlayerEntity player = client.player;
        if (holdTimer < 0) {
            return;
        }
        holdTimer--;
        if (holdTimer < 0 && player != null) {
            select(client, player, originalSlot);
        }
    }

    public static void setArmed(List<Integer> ids) {
        ARMED.clear();
        ARMED.addAll(ids);
    }

    public static boolean isArmed(int id) {
        return ARMED.contains(id);
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

    public static PlayerBodyEntity findImplantableCorpse(PlayerEntity player) {
        PlayerBodyEntity corpse = findCorpse(player);
        return corpse != null && !isArmed(corpse.getId()) ? corpse : null;
    }

    public static PlayerBodyEntity findCorpse(PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(RANGE));
        Box box = player.getBoundingBox().stretch(look.multiply(RANGE)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityCollision(player.getWorld(), player, start, end, box,
                entity -> entity instanceof PlayerBodyEntity, MARGIN);
        return hit != null && hit.getEntity() instanceof PlayerBodyEntity body ? body : null;
    }

    private static void select(MinecraftClient client, PlayerEntity player, int slot) {
        player.getInventory().selectedSlot = slot;
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler != null) {
            handler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }

    private static int findGrenade(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(WatheItems.GRENADE)) {
                return i;
            }
        }
        return -1;
    }
}
