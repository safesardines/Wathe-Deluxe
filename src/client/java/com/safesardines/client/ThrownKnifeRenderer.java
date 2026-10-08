package com.safesardines.client;

import com.safesardines.ThrownKnifeEntity;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ThrownKnifeRenderer extends EntityRenderer<ThrownKnifeEntity> {
    private final ItemRenderer itemRenderer;

    public ThrownKnifeRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownKnifeEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(1.5F, 1.5F, 1.5F);
        float yawAngle = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        float pitchAngle = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());
        if (entity.isLodged()) {
            Direction face = entity.getLodgedFace();
            yawAngle = (float) (MathHelper.atan2(-face.getOffsetX(), -face.getOffsetZ()) * 57.2957763671875);
            pitchAngle = (float) (MathHelper.atan2(-face.getOffsetY(), MathHelper.sqrt((float) (face.getOffsetX() * face.getOffsetX() + face.getOffsetZ() * face.getOffsetZ()))) * 57.2957763671875);
        }
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawAngle));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitchAngle));
        if (!entity.isLodged()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((entity.age + tickDelta) * 30.0F));
        }
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45.0F));
        this.itemRenderer.renderItem(new ItemStack(WatheItems.KNIFE), ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(ThrownKnifeEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
