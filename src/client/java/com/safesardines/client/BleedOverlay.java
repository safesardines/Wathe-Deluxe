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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BleedOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger("wathedeluxe");
    private static final Identifier BLUR = Identifier.of("wathedeluxe", "shaders/post/blur.json");
    private static final Identifier NAUSEA = Identifier.ofVanilla("textures/misc/nausea.png");
    private static final Identifier WHITE = Identifier.ofVanilla("textures/misc/white.png");
    private static final int FADE_TICKS = 20;
    private static final float MAX_RADIUS = 40.0F;
    private static int debugCounter;
    private static boolean blurLoaded;
    private static boolean active;
    private static int remaining;
    private static int total;
    private static int fadeTicks;
    private static int serverTicks = -1;

    private BleedOverlay() {
    }

    public static void tick(MinecraftClient client) {
        PlayerEntity player = client.player;
        PlayerBleedingComponent component = player == null ? null : PlayerBleedingComponent.KEY.get(player);
        int ticks = component == null ? -1 : component.bleedingTicks;
        if (ticks > 0) {
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
            float spread = progress() * fade();
            processor.setUniforms("Radius", spread * MAX_RADIUS);
            processor.setUniforms("Spread", spread);
            if (debugCounter++ % 20 == 0) {
                LOGGER.info("[wathedeluxe] blur spread={} radius={} remaining={} total={}",
                        spread, spread * MAX_RADIUS, remaining, total);
            }
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
        return buildup(2.5F) * fade();
    }

    private static float screenLevel() {
        return buildup(3.0F) * fade();
    }

    private static float buildup(float power) {
        float s = progress();
        s = s * s * (3.0F - 2.0F * s);
        return (float) Math.pow(s, power);
    }

    private static float progress() {
        if (!active || total <= 0) {
            return 0.0F;
        }
        float p = 1.0F - (float) remaining / total;
        return p < 0.0F ? 0.0F : (p > 1.0F ? 1.0F : p);
    }

    private static float fade() {
        return (float) fadeTicks / FADE_TICKS;
    }

    public static void renderOverlay(DrawContext context, RenderTickCounter tickCounter) {
        float red = redLevel();
        if (red <= 0.01F) {
            return;
        }
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.ZERO,
                GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);
        context.setShaderColor(red * 0.8F, red, red, 1.0F);
        context.drawTexture(NAUSEA, 0, 0, 0, 0, width, height, width, height);

        float screen = screenLevel();
        context.setShaderColor(screen * 0.25F, screen * 0.7F, screen * 0.7F, 1.0F);
        context.drawTexture(WHITE, 0, 0, 0.0F, 0.0F, width, height, 4, 4);

        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
