package com.safesardines.client.mixin;

import com.safesardines.RazorRoles;
import com.safesardines.ShivPayload;
import com.safesardines.ThrownKnifeEntity;
import com.safesardines.client.ShivController;
import com.safesardines.client.wathedeluxeClient;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class RazorHudMixin {
    private static float reclaimAlpha;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void razorHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        PlayerEntity player = client.player;
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
            return;
        }
        if (!GameWorldComponent.KEY.get(player.getWorld()).isRole(player, RazorRoles.RAZOR)) {
            return;
        }
        if (ShivPayload.hasKnife(player)) {
            Text line;
            int color;
            if (ShivController.hasSeconds()) {
                line = Text.translatable("tip.wathedeluxe.razor.cooldown", ShivController.cooldownSeconds());
                color = 0xFF5555;
            } else if (player.getItemCooldownManager().isCoolingDown(WatheItems.KNIFE)) {
                line = Text.literal("Knife is on cooldown");
                color = 0xFF5555;
            } else {
                line = Text.translatable("tip.wathedeluxe.razor",
                        wathedeluxeClient.razorAbility.getBoundKeyLocalizedText().copy().styled(style -> style.withColor(0x55FF55)))
                        .styled(style -> style.withColor(0x00AA00))
                        .append(Text.translatable("tip.wathedeluxe.razor.suffix").styled(style -> style.withColor(0x55FF55)));
                color = 0x00AA00;
            }
            int drawY = context.getScaledWindowHeight() - this.getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(this.getTextRenderer(), line, context.getScaledWindowWidth() - this.getTextRenderer().getWidth(line), drawY, color);
        }
        boolean showReclaim = player != null && ThrownKnifeEntity.isOwnKnifeTargeted(player, 3.0);
        float fade = tickCounter.getLastFrameDuration() / 4.0F;
        reclaimAlpha = MathHelper.lerp(fade, reclaimAlpha, showReclaim ? 1.0F : 0.0F);
        if (reclaimAlpha > 0.05F) {
            Text label = Text.translatable("tip.wathedeluxe.razor.reclaim");
            int width = this.getTextRenderer().getWidth(label);
            int alpha = (int) (reclaimAlpha * 255.0F) << 24;
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
            context.getMatrices().scale(0.6F, 0.6F, 1.0F);
            context.getMatrices().translate(0.0F, 20.0F + this.getTextRenderer().fontHeight + 8.0F, 0.0F);
            context.drawTextWithShadow(this.getTextRenderer(), label, -width / 2, 0, alpha | 0xAAAAAA);
            Text sub = Text.translatable("tip.wathedeluxe.razor.reclaim.only_you",
                    Text.translatable("announcement.role.wathedeluxe.razor").copy().styled(style -> style.withColor(0x555555)));
            int subWidth = this.getTextRenderer().getWidth(sub);
            context.getMatrices().translate(0.0F, this.getTextRenderer().fontHeight + 4.0F, 0.0F);
            context.getMatrices().scale(0.833F, 0.833F, 1.0F);
            context.drawTextWithShadow(this.getTextRenderer(), sub, -subWidth / 2, 0, alpha | 0x777777);
            context.getMatrices().pop();
        }
    }
}
