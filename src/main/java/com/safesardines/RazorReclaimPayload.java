package com.safesardines;

import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public record RazorReclaimPayload() implements CustomPayload {
    public static final CustomPayload.Id<RazorReclaimPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "razor_reclaim"));
    public static final PacketCodec<RegistryByteBuf, RazorReclaimPayload> CODEC = PacketCodec.unit(new RazorReclaimPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<RazorReclaimPayload> {
        @Override
        public void receive(RazorReclaimPayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                return;
            }
            ThrownKnifeEntity knife = null;
            for (ThrownKnifeEntity nearby : player.getWorld().getEntitiesByClass(ThrownKnifeEntity.class, player.getBoundingBox().expand(3.0),
                    nearby -> nearby.isLodged() && player.getUuid().equals(nearby.getThrowerUuid()))) {
                if (knife == null || player.squaredDistanceTo(knife) > player.squaredDistanceTo(nearby)) {
                    knife = nearby;
                }
            }
            if (knife == null) {
                return;
            }
            player.giveItemStack(new ItemStack(WatheItems.KNIFE));
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.3F, 1.0F);
            knife.discard();
        }
    }
}
