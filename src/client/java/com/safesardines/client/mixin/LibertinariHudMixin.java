package com.safesardines.client.mixin;

import com.safesardines.LibertinariRoles;
import com.safesardines.client.LibertinariController;
import com.safesardines.client.wathedeluxeClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameFunctions;
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
public abstract class LibertinariHudMixin {
    private static final int BASE_COLOR = 0xAAAAAA;
    private static final int ACCENT_COLOR = 0x7A5A55;
    private static final int READY_COLOR = 0x00AA00;
    private static final int READY_KEY_COLOR = 0x55FF55;
    private static final int COOLDOWN_COLOR = 0xFF5555;
    private static float implantAlpha;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void libertinariHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }
        PlayerEntity player = client.player;
        if (!GameFunctions.isPlayerAliveAndSurvival(player) || !LibertinariRoles.isLibertinari(player)) {
            return;
        }
        TextRenderer textRenderer = this.getTextRenderer();
        Text key = wathedeluxeClient.ability.getBoundKeyLocalizedText();
        PlayerBodyEntity target = LibertinariController.findImplantableCorpse(player);

        Text line;
        int color;
        if (LibertinariController.hasSeconds()) {
            line = Text.translatable("tip.wathedeluxe.libertinari.cooldown", LibertinariController.cooldownSeconds());
            color = COOLDOWN_COLOR;
        } else if (target != null) {
            line = Text.translatable("tip.wathedeluxe.libertinari.implant", key.copy().styled(style -> style.withColor(READY_KEY_COLOR))).styled(style -> style.withColor(READY_COLOR));
            color = READY_COLOR;
        } else {
            line = Text.translatable("tip.wathedeluxe.libertinari.detonate", key.copy().styled(style -> style.withColor(READY_KEY_COLOR))).styled(style -> style.withColor(READY_COLOR));
            color = READY_COLOR;
        }
        int drawY = context.getScaledWindowHeight() - textRenderer.getWrappedLinesHeight(line, 999999);
        context.drawTextWithShadow(textRenderer, line, context.getScaledWindowWidth() - textRenderer.getWidth(line), drawY, color);

        boolean showImplant = target != null && !LibertinariController.hasSeconds();
        float fade = tickCounter.getLastFrameDuration() / 4.0F;
        implantAlpha = MathHelper.lerp(fade, implantAlpha, showImplant ? 1.0F : 0.0F);
        if (implantAlpha <= 0.05F) {
            return;
        }
        Text label = Text.translatable("tip.wathedeluxe.libertinari.implant", key);
        Text sub = Text.translatable("tip.wathedeluxe.libertinari.detonate_hint");
        int alpha = (int) (implantAlpha * 255.0F) << 24;
        int width = textRenderer.getWidth(label);
        int subWidth = textRenderer.getWidth(sub);
        context.getMatrices().push();
        context.getMatrices().translate(context.getScaledWindowWidth() / 2.0F, context.getScaledWindowHeight() / 2.0F + 6.0F, 0.0F);
        context.getMatrices().scale(0.6F, 0.6F, 1.0F);
        context.getMatrices().translate(0.0F, 20.0F + textRenderer.fontHeight + 8.0F, 0.0F);
        context.drawTextWithShadow(textRenderer, label, -width / 2, 0, alpha | BASE_COLOR);
        context.getMatrices().translate(0.0F, textRenderer.fontHeight + 4.0F, 0.0F);
        context.getMatrices().scale(0.833F, 0.833F, 1.0F);
        context.drawTextWithShadow(textRenderer, sub, -subWidth / 2, 0, alpha | ACCENT_COLOR);
        context.getMatrices().pop();
    }
}
