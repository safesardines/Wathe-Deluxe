package com.safesardines;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record WathedeluxeConfigPayload(boolean destruction, boolean placement, boolean collisionsWhileWalking, boolean breakAnything, boolean itemDrops, boolean placeAnything, float libertinariDiscount, boolean timeDiscount, float maxDiscount, boolean uncapped) implements CustomPayload {
    public static final CustomPayload.Id<WathedeluxeConfigPayload> ID = new CustomPayload.Id<>(Identifier.of("wathedeluxe", "config"));
    public static final PacketCodec<RegistryByteBuf, WathedeluxeConfigPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeBoolean(payload.destruction());
                buf.writeBoolean(payload.placement());
                buf.writeBoolean(payload.collisionsWhileWalking());
                buf.writeBoolean(payload.breakAnything());
                buf.writeBoolean(payload.itemDrops());
                buf.writeBoolean(payload.placeAnything());
                buf.writeFloat(payload.libertinariDiscount());
                buf.writeBoolean(payload.timeDiscount());
                buf.writeFloat(payload.maxDiscount());
                buf.writeBoolean(payload.uncapped());
            },
            buf -> new WathedeluxeConfigPayload(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readBoolean(), buf.readFloat(), buf.readBoolean()));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<WathedeluxeConfigPayload> {
        @Override
        public void receive(WathedeluxeConfigPayload payload, ServerPlayNetworking.Context context) {
            ServerPlayerEntity player = context.player();
            if (!player.hasPermissionLevel(2)) {
                return;
            }
            WathedeluxeConfig config = WathedeluxeConfig.HANDLER.instance();
            config.destruction = payload.destruction();
            config.placement = payload.placement();
            config.collisionsWhileWalking = payload.collisionsWhileWalking();
            config.breakAnything = payload.breakAnything();
            config.itemDrops = payload.itemDrops();
            config.placeAnything = payload.placeAnything();
            config.libertinariDiscount = payload.libertinariDiscount();
            config.timeDiscount = payload.timeDiscount();
            config.maxDiscount = payload.maxDiscount();
            config.uncapped = payload.uncapped();
        }
    }
}
