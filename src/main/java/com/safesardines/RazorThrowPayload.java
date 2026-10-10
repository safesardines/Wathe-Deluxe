package com.safesardines;

import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public record RazorThrowPayload() implements CustomPayload {
    public static final CustomPayload.Id<RazorThrowPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "razor_throw"));
    public static final PacketCodec<RegistryByteBuf, RazorThrowPayload> CODEC = PacketCodec.unit(new RazorThrowPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<RazorThrowPayload> {
        @Override
        public void receive(RazorThrowPayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                return;
            }
            if (!GameWorldComponent.KEY.get(player.getWorld()).isRole(player, RazorRoles.RAZOR)) {
                return;
            }
            if (player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)) {
                return;
            }
            ItemStack stack = player.getActiveItem();
            Hand hand = player.getActiveHand();
            if (player.isUsingItem() && stack.isOf(WatheItems.KNIFE)) {
                player.stopUsingItem();
            } else if (player.getMainHandStack().isOf(WatheItems.KNIFE)) {
                stack = player.getMainHandStack();
                hand = Hand.MAIN_HAND;
            } else if (player.getOffHandStack().isOf(WatheItems.KNIFE)) {
                stack = player.getOffHandStack();
                hand = Hand.OFF_HAND;
            } else {
                return;
            }
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            ThrownKnifeEntity knife = new ThrownKnifeEntity(player.getWorld(), player);
            knife.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 3.5F, 0.0F);
            knife.aim();
            if (player.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnEntity(knife);
            }
            player.swingHand(hand);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvent.of(Identifier.of("wathedeluxe", "knife_throw")), SoundCategory.PLAYERS, 1.0F, 1.0F);
            GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
            if (!player.isCreative() && game.getGameMode() != WatheGameModes.LOOSE_ENDS) {
                player.getItemCooldownManager().set(WatheItems.KNIFE, GameConstants.ITEM_COOLDOWNS.get(WatheItems.KNIFE));
            }
        }
    }
}
