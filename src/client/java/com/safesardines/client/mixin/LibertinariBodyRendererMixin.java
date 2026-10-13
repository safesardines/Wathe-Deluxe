package com.safesardines.client.mixin;

import com.safesardines.client.LibertinariController;
import dev.doctor4t.wathe.client.model.entity.PlayerSkeletonEntityModel;
import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(PlayerBodyEntityRenderer.class)
public class LibertinariBodyRendererMixin {
    private static ModelPart lump;

    @Inject(method = "render(Ldev/doctor4t/wathe/entity/PlayerBodyEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/model/BipedEntityModel;Lnet/minecraft/client/render/RenderLayer;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", shift = At.Shift.AFTER))
    private void libertinari$bodyLump(PlayerBodyEntity entity, float f, float g, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityModel<PlayerBodyEntity> model, RenderLayer layer, float scale, float alpha, CallbackInfo ci) {
        if (alpha <= 0.0F || layer == null || !LibertinariController.isArmed(entity.getId())) {
            return;
        }
        int color = new Color(1.0F, 1.0F, 1.0F, alpha).getRGB();
        matrices.push();
        model.body.rotate(matrices);
        if (model instanceof PlayerSkeletonEntityModel) {
            ItemStack stack = WatheItems.GRENADE.getDefaultStack();
            matrices.translate(0.0F, 8.0F / 16.0F, 0.5F / 16.0F);
            matrices.scale(0.6F, 0.6F, 0.6F);
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
        } else {
            lump().render(matrices, vertexConsumers.getBuffer(layer), light, OverlayTexture.DEFAULT_UV, color);
        }
        matrices.pop();
    }

    private static ModelPart lump() {
        if (lump == null) {
            ModelData data = new ModelData();
            data.getRoot().addChild("lump",
                    ModelPartBuilder.create().uv(26, 22).cuboid(-2.0F, 4.0F, 1.0F, 4.0F, 4.0F, 2.0F),
                    ModelTransform.pivot(0.0F, 0.0F, 0.0F));
            lump = TexturedModelData.of(data, 64, 64).createModel();
        }
        return lump;
    }
}
