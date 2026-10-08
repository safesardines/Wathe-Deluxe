package com.safesardines.client;

import com.safesardines.*;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.util.WatheItemTooltips;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class wathedeluxeClient implements ClientModInitializer {
    public static KeyBinding razorAbility;
    public static int razorCooldownTicks;
    // i don't know how to do it properly sorry
    private static boolean attackWasDown;
    private static boolean useWasDown;

    @Override
    public void onInitializeClient() {
        razorAbility = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.wathedeluxe.razor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F, "category.wathe.keybinds"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (razorCooldownTicks > 0) {
                razorCooldownTicks--;
            }
            if (razorAbility.wasPressed()) {
                client.execute(() -> {
                    PlayerEntity player = client.player;
                    if (player == null || !GameFunctions.isPlayerAliveAndSurvival(player)) {
                        return;
                    }
                    if (!GameWorldComponent.KEY.get(player.getWorld()).isRole(player, RazorRoles.RAZOR)) {
                        return;
                    }
                    if (!player.getMainHandStack().isEmpty()) {
                        return;
                    }
                    if (!RazorStabPayload.hasKnife(player)) {
                        return;
                    }
                    razorCooldownTicks = (int) (GameConstants.ITEM_COOLDOWNS.get(WatheItems.KNIFE) * 1.5);
                    ClientPlayNetworking.send(new RazorStabPayload());
                });
            }
            boolean attackDown = client.options.attackKey.isPressed();
            if (attackDown && !attackWasDown && client.currentScreen == null) {
                PlayerEntity player = client.player;
                if (player != null && GameFunctions.isPlayerAliveAndSurvival(player)
                        && GameWorldComponent.KEY.get(player.getWorld()).isRole(player, RazorRoles.RAZOR)
                        && player.isUsingItem() && player.getActiveItem().isOf(WatheItems.KNIFE)
                        && !player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)) {
                    ClientPlayNetworking.send(new RazorThrowPayload());
                    player.swingHand(player.getActiveHand());
                    player.stopUsingItem();
                }
            }
            attackWasDown = attackDown;
            boolean useDown = client.options.useKey.isPressed();
            if (useDown && !useWasDown && client.currentScreen == null) {
                PlayerEntity player = client.player;
                if (player != null && GameFunctions.isPlayerAliveAndSurvival(player)
                        && ThrownKnifeEntity.isOwnKnifeTargeted(player, 3.0)) {
                    ClientPlayNetworking.send(new RazorReclaimPayload());
                }
            }
            useWasDown = useDown;
        });
        EntityRendererRegistry.register(RazorRoles.THROWN_KNIFE, ThrownKnifeRenderer::new);
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            if (!stack.isOf(WatheItems.KNIFE)) {
                return;
            }
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null || MinecraftClient.getInstance().world == null) {
                return;
            }
            if (!GameWorldComponent.KEY.get(player.getWorld()).isRole(player, RazorRoles.RAZOR)) {
                return;
            }
            lines.add(Text.translatable("tip.wathedeluxe.razor.throw").styled(style -> style.withColor(WatheItemTooltips.REGULAR_TOOLTIP_COLOR)));
        });
    }
}
