package com.safesardines;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public class ThrownKnifeEntity extends ThrownItemEntity {
    private static final TrackedData<Boolean> LODGED = DataTracker.registerData(ThrownKnifeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Optional<UUID>> THROWER = DataTracker.registerData(ThrownKnifeEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Byte> FACE = DataTracker.registerData(ThrownKnifeEntity.class, TrackedDataHandlerRegistry.BYTE);

    public ThrownKnifeEntity(EntityType<ThrownKnifeEntity> type, World world) {
        super(type, world);
    }

    public ThrownKnifeEntity(World world, LivingEntity owner) {
        super(RazorRoles.THROWN_KNIFE, owner, world);
        this.dataTracker.set(THROWER, Optional.of(owner.getUuid()));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LODGED, false);
        builder.add(THROWER, Optional.empty());
        builder.add(FACE, (byte) 0);
    }

    @Override
    protected Item getDefaultItem() {
        return WatheItems.KNIFE;
    }

    public boolean isLodged() {
        return this.dataTracker.get(LODGED);
    }

    public UUID getThrowerUuid() {
        return this.dataTracker.get(THROWER).orElse(null);
    }

    public Direction getLodgedFace() {
        return Direction.values()[this.dataTracker.get(FACE) & 0xFF];
    }

    public void aim() {
        this.bakeRotation();
    }

    private void bakeRotation() {
        Vec3d motion = this.getVelocity();
        if (motion.lengthSquared() > 1.0E-6) {
            this.setYaw((float) (MathHelper.atan2(motion.x, motion.z) * 57.2957763671875));
            this.setPitch((float) (MathHelper.atan2(motion.y, motion.horizontalLength()) * 57.2957763671875));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Lodged", this.isLodged());
        nbt.putByte("Face", this.dataTracker.get(FACE));
        if (this.getThrowerUuid() != null) {
            nbt.putUuid("Thrower", this.getThrowerUuid());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(LODGED, nbt.getBoolean("Lodged"));
        this.dataTracker.set(FACE, nbt.getByte("Face"));
        if (nbt.contains("Thrower")) {
            this.dataTracker.set(THROWER, Optional.of(nbt.getUuid("Thrower")));
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        if (!this.getWorld().isClient()) {
            if (hit.getEntity() instanceof PlayerEntity victim) {
                Entity owner = this.getOwner();
                GameFunctions.killPlayer(victim, true, owner instanceof PlayerEntity player ? player : null, GameConstants.DeathReasons.KNIFE);
                victim.playSound(WatheSounds.ITEM_KNIFE_STAB, 1.0F, 1.0F);
            }
            this.dropStack(new ItemStack(WatheItems.KNIFE));
            this.discard();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        super.onBlockHit(hit);
        if (!this.getWorld().isClient()) {
            this.bakeRotation();
            Vec3d face = Vec3d.of(hit.getSide().getVector());
            Vec3d pos = hit.getPos().add(face.multiply(0.25));
            BlockState state = this.getWorld().getBlockState(hit.getBlockPos());
            this.getWorld().playSound(null, this.getBlockPos(), state.getSoundGroup().getHitSound(), SoundCategory.BLOCKS, 1.4F, 1.0F);
            Vec3d at = hit.getPos().add(face.multiply(0.2));
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), at.x, at.y, at.z, 8, 0.1, 0.1, 0.1, 0.1);
            }
            this.setPosition(pos.x, pos.y, pos.z);
            this.setVelocity(0.0, 0.0, 0.0);
            this.setNoGravity(true);
            this.dataTracker.set(LODGED, true);
            this.dataTracker.set(FACE, (byte) hit.getSide().ordinal());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isLodged() && !this.getWorld().isClient()) {
            this.updateRotation();
        }
    }

    public static boolean isOwnKnifeTargeted(PlayerEntity player, double range) {
        return ProjectileUtil.getCollision(player, entity -> entity instanceof ThrownKnifeEntity knife
                && knife.isLodged() && player.getUuid().equals(knife.getThrowerUuid()), range) instanceof EntityHitResult;
    }
}
