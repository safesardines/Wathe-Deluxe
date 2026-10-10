package com.safesardines.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.safesardines.PlayerBleedingComponent;
import com.safesardines.client.mixin.GameRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class BleedOverlay {
    private static final Identifier BLUR = Identifier.of("wathedeluxe", "shaders/post/blur.json");
    private static final Identifier NAUSEA = Identifier.ofVanilla("textures/misc/nausea.png");
    private static final int FADE_TICKS = 20;
    private static final float MAX_RADIUS = 12.0F;
    private static boolean blurLoaded;
    private static boolean active;
    private static int remaining;
    private static int total;
    private static int fadeTicks;
    private static int serverTicks = -1;
    private static int dyingRemaining;
    private static int dyingTotal;

    private BleedOverlay() {
    }

    public static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;
        PlayerBleedingComponent component = player == null ? null : PlayerBleedingComponent.KEY.get(player);
        int ticks = component == null ? -1 : component.bleedingTicks;
        int dying = component == null ? -1 : component.dyingTicks;
        if (dying > 0) {
            if (dyingRemaining <= 0) {
                dyingTotal = dying;
            }
            active = true;
            dyingRemaining = dying;
            fadeTicks = FADE_TICKS;
            applyBlur(client);
        } else if (ticks > 0) {
            dyingRemaining = 0;
            dyingTotal = 0;
            if (!active || ticks != serverTicks) {
                active = true;
                remaining = ticks;
                total = component.initialBleedingTicks;
                serverTicks = ticks;
            }
            if (remaining > 0) {
                remaining--;
            }
            fadeTicks = FADE_TICKS;
            applyBlur(client);
        } else if (active) {
            remaining = 0;
            fadeTicks--;
            if (fadeTicks <= 0) {
                active = false;
                total = 0;
                serverTicks = -1;
                dyingRemaining = 0;
                dyingTotal = 0;
                disableBlur(client);
            } else {
                applyBlur(client);
            }
        } else if (blurLoaded) {
            disableBlur(client);
        }
    }

    private static void applyBlur(MinecraftClient client) {
        GameRenderer renderer = client.gameRenderer;
        PostEffectProcessor processor = renderer.getPostProcessor();
        if (processor == null || !blurLoaded) {
            ((GameRendererAccessor) renderer).wathedeluxe$loadPostProcessor(BLUR);
            processor = renderer.getPostProcessor();
            blurLoaded = true;
        }
        if (processor != null) {
            processor.setUniforms("Radius", intensity() * MAX_RADIUS);
        }
    }

    private static void disableBlur(MinecraftClient client) {
        if (blurLoaded) {
            client.gameRenderer.disablePostProcessor();
            blurLoaded = false;
        }
    }

    public static float intensity() {
        return buildup(1.5F) * fade();
    }

    public static float redLevel() {
        return buildup(1.0F) * fade();
    }

    public static float staticLevel() {
        return buildup(2.0F) * fade();
    }

    private static float buildup(float power) {
        float s = progress();
        s = s * s * (3.0F - 2.0F * s);
        return (float) Math.pow(s, power);
    }

    private static float progress() {
        if (!active) {
            return 0.0F;
        }
        if (dyingRemaining > 0) {
            return 1.0F;
        }
        if (total <= 0) {
            return 0.0F;
        }
        float p = 1.0F - (float) remaining / total;
        return p < 0.0F ? 0.0F : (p > 1.0F ? 1.0F : p);
    }

    public static float dyingProgress() {
        if (dyingTotal <= 0 || dyingRemaining <= 0) {
            return 0.0F;
        }
        float d = 1.0F - (float) dyingRemaining / dyingTotal;
        return d < 0.0F ? 0.0F : (d > 1.0F ? 1.0F : d);
    }

    private static float fade() {
        return (float) fadeTicks / FADE_TICKS;
    }

    public static void renderOverlay(DrawContext context, RenderTickCounter tickCounter) {
        float red = redLevel();
        float dying = dyingProgress();
        if (red <= 0.01F && dying <= 0.01F) {
            return;
        }
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        float scale = 1.0F + dying * 2.5F;
        int dw = (int) (width * scale);
        int dh = (int) (height * scale);
        int dx = (width - dw) / 2;
        int dy = (height - dh) / 2;
        float g = Math.min(1.0F, red + dying);
        float r = Math.min(1.0F, red * 0.8F + dying);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.ZERO,
                GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);
        context.setShaderColor(r, g, g, 1.0F);
        context.drawTexture(NAUSEA, dx, dy, 0, 0, dw, dh, dw, dh);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
