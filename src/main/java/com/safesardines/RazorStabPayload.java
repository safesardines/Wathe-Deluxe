package com.safesardines;

import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.item.KnifeItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public record RazorStabPayload() implements CustomPayload {
    public static final CustomPayload.Id<RazorStabPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "razor_stab"));
    public static final PacketCodec<RegistryByteBuf, RazorStabPayload> CODEC = PacketCodec.unit(new RazorStabPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static boolean hasKnife(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(WatheItems.KNIFE)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBackstabTarget(PlayerEntity player, PlayerEntity target) {
        Vec3d look = target.getRotationVector();
        Vec3d flatLook = new Vec3d(look.x, 0.0, look.z).normalize();
        Vec3d toAttacker = new Vec3d(player.getX() - target.getX(), 0.0, player.getZ() - target.getZ()).normalize();
        return flatLook.dotProduct(toAttacker) < -0.5;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<RazorStabPayload> {
        @Override
        public void receive(RazorStabPayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                return;
            }
            GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
            if (!game.isRole(player, RazorRoles.RAZOR)) {
                return;
            }
            if (!player.getMainHandStack().isEmpty()) {
                return;
            }
            if (!hasKnife(player)) {
                return;
            }
            if (player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)) {
                return;
            }
            HitResult hit = KnifeItem.getKnifeTarget(player);
            if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof PlayerEntity target)) {
                return;
            }
            if (target.squaredDistanceTo(player) > 9.0) {
                return;
            }
            if (!isBackstabTarget(player, target)) {
                return;
            }
            GameFunctions.killPlayer(target, true, player, GameConstants.DeathReasons.KNIFE);
            target.playSound(WatheSounds.ITEM_KNIFE_STAB, 1.0F, 1.0F);
            player.swingHand(Hand.MAIN_HAND);
            if (!player.isCreative() && game.getGameMode() != WatheGameModes.LOOSE_ENDS) {
                player.getItemCooldownManager().set(WatheItems.KNIFE, (int) (GameConstants.ITEM_COOLDOWNS.get(WatheItems.KNIFE) * 1.5));
            }
        }
    }
}
