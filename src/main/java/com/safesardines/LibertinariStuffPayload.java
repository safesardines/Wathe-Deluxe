package com.safesardines;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
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
import net.minecraft.util.Identifier;

public record LibertinariStuffPayload() implements CustomPayload {
    public static final CustomPayload.Id<LibertinariStuffPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "libertinari_stuff"));
    public static final PacketCodec<RegistryByteBuf, LibertinariStuffPayload> CODEC = PacketCodec.unit(new LibertinariStuffPayload());
    private static final double RANGE = 3.0;

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<LibertinariStuffPayload> {
        @Override
        public void receive(LibertinariStuffPayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!GameFunctions.isPlayerAliveAndSurvival(player) || !LibertinariRoles.isLibertinari(player)) {
                return;
            }
            if (!LibertinariCooldowns.ready(player)) {
                return;
            }
            if (!(player.getWorld() instanceof ServerWorld world)) {
                return;
            }
            PlayerBodyEntity body = null;
            double best = Double.MAX_VALUE;
            for (PlayerBodyEntity candidate : world.getEntitiesByClass(PlayerBodyEntity.class, player.getBoundingBox().expand(RANGE), entity -> true)) {
                double distance = player.squaredDistanceTo(candidate);
                if (distance < best) {
                    best = distance;
                    body = candidate;
                }
            }
            if (body == null || LibertinariCorpses.isArmed(body)) {
                return;
            }
            if (!consumeGrenade(player)) {
                return;
            }
            LibertinariCorpses.arm(body, player.getUuid());
            LibertinariCorpses.sync(player.getServer());
            world.playSound(null, body.getBlockPos(), SoundEvent.of(Identifier.of("wathedeluxe", "libertinari_squish")), SoundCategory.PLAYERS, 1.0F, 1.0F);
            LibertinariCooldowns.trigger(player, LibertinariCooldowns.IMPLANT_COOLDOWN);
        }
    }

    private static boolean consumeGrenade(ServerPlayerEntity player) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(WatheItems.GRENADE)) {
                stack.decrement(1);
                return true;
            }
        }
        return false;
    }
}
