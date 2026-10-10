package com.safesardines;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record ShivPayload() implements CustomPayload {
    public static final CustomPayload.Id<ShivPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "shiv"));
    public static final PacketCodec<RegistryByteBuf, ShivPayload> CODEC = PacketCodec.unit(new ShivPayload());
    private static final double RANGE = 1.0;

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static boolean hasKnife(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(WatheItems.KNIFE)) {
                return true;
            }
        }
        return false;
    }

    public static PlayerEntity findTarget(PlayerEntity player) {
        Vec3d look = player.getRotationVec(1.0F);
        PlayerEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (PlayerEntity other : player.getWorld().getPlayers()) {
            if (other == player || !GameFunctions.isPlayerAliveAndSurvival(other)) {
                continue;
            }
            double dx = other.getX() - player.getX();
            double dz = other.getZ() - player.getZ();
            if (dx * dx + dz * dz > RANGE * RANGE) {
                continue;
            }
            Vec3d to = new Vec3d(dx, (other.getY() + other.getHeight() * 0.5) - player.getEyeY(), dz);
            double distance = to.length();
            if (distance > RANGE + 0.5 || look.dotProduct(to.normalize()) < 0.5) {
                continue;
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                best = other;
            }
        }
        return best;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<ShivPayload> {
        @Override
        public void receive(ShivPayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                return;
            }
            GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
            if (!game.isRole(player, RazorRoles.RAZOR)) {
                return;
            }
            if (!hasKnife(player)) {
                return;
            }
            if (player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)) {
                return;
            }
            PlayerEntity victim = findTarget(player);
            if (victim == null) {
                return;
            }
            PlayerBleedingComponent component = PlayerBleedingComponent.KEY.get(victim);
            component.shivCount++;
            int seconds = Math.max(7, (int) Math.ceil(25.0 / Math.pow(2, component.shivCount - 1)));
            Vec3d local = new Vec3d(0.0, victim.getHeight() * 0.5, 0.0);
            component.setBleeding(seconds * 20, player.getUuid(), (float) local.x, (float) local.y, (float) local.z);
            victim.playSound(WatheSounds.ITEM_KNIFE_STAB, 1.0F, 1.0F);
            player.swingHand(Hand.MAIN_HAND);
            ShivCooldowns.onShiv(player.getUuid(), player.getServer().getTicks());
        }
    }
}
