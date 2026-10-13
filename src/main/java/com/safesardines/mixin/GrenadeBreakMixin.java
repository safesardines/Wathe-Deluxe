package com.safesardines.mixin;

import com.safesardines.LibertinariCorpses;
import com.safesardines.TemporaryBlocks;
import dev.doctor4t.wathe.entity.GrenadeEntity;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrenadeEntity.class)
public class GrenadeBreakMixin {
    @Inject(method = "onCollision", at = @At("TAIL"))
    private void wathedeluxe$blastEffects(HitResult hitResult, CallbackInfo ci) {
        GrenadeEntity self = (GrenadeEntity) (Object) this;
        if (self.getWorld().isClient()) {
            return;
        }
        ServerWorld world = (ServerWorld) self.getWorld();
        BlockPos center = BlockPos.ofFloored(self.getPos());
        for (BlockPos pos : BlockPos.iterate(center.add(-2, -2, -2), center.add(2, 2, 2))) {
            if (pos.getSquaredDistance(center) > 4.0) {
                continue;
            }
            BlockState state = world.getBlockState(pos);
            if ((state.isIn(TemporaryBlocks.FRAGILE) || !state.isFullCube(world, pos))
                    && !state.isIn(BlockTags.STAIRS) && !state.isIn(BlockTags.SLABS) && !state.isIn(BlockTags.WALLS)
                    && !state.isIn(BlockTags.FENCES) && !state.isIn(BlockTags.BEDS)) {
                TemporaryBlocks.breakTemporary(world, pos);
            }
        }
        LibertinariCorpses.triggerAround(world, self.getBoundingBox().expand(3f));
    }
}
