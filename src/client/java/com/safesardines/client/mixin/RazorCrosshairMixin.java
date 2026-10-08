package com.safesardines.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.safesardines.RazorRoles;
import com.safesardines.RazorStabPayload;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.item.KnifeItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(dev.doctor4t.wathe.client.gui.CrosshairRenderer.class)
public class RazorCrosshairMixin {
    private static final Identifier CROSSHAIR_TARGET = Identifier.of("wathe", "hud/crosshair_target");

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private static void wathedeluxe$razorTarget(MinecraftClient client, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (player == null) {
            return;
        }
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
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
        if (player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)) {
            return;
        }
        HitResult hit = KnifeItem.getKnifeTarget(player);
        if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof PlayerEntity target)) {
            return;
        }
        if (!RazorStabPayload.isBackstabTarget(player, target)) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.DstFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.SrcFactor.ZERO,
                GlStateManager.DstFactor.ONE);
        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() / 2.0F, 0.0F);
        context.getMatrices().translate(-1.5F, -1.5F, 0.0F);
        context.drawGuiTexture(CROSSHAIR_TARGET, 0, 0, 3, 3);
        context.getMatrices().pop();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
