package com.safesardines;

import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheParticles;
import dev.doctor4t.wathe.index.WatheSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class LibertinariCorpses {
    private static final double EXPLOSION_Y = 0.05;
    private static final int CHAIN_DELAY = 8;
    private static final List<Pending> ARMED = new ArrayList<>();
    private static final List<Scheduled> SCHEDULED = new ArrayList<>();
    private static boolean bloodResolved;
    private static ParticleEffect blood;

    private static final class Pending {
        final PlayerBodyEntity body;
        final UUID owner;

        Pending(PlayerBodyEntity body, UUID owner) {
            this.body = body;
            this.owner = owner;
        }
    }

    private static final class Scheduled {
        final ServerWorld world;
        final PlayerBodyEntity body;
        final UUID owner;
        int ticks;

        Scheduled(ServerWorld world, PlayerBodyEntity body, UUID owner, int ticks) {
            this.world = world;
            this.body = body;
            this.owner = owner;
            this.ticks = ticks;
        }
    }

    public static boolean isArmed(PlayerBodyEntity body) {
        prune();
        for (Pending pending : ARMED) {
            if (pending.body == body) {
                return true;
            }
        }
        return false;
    }

    public static void arm(PlayerBodyEntity body, UUID owner) {
        if (!isArmed(body)) {
            ARMED.add(new Pending(body, owner));
        }
    }

    public static boolean detonate(ServerPlayerEntity player) {
        Pending target = findTarget(player);
        if (target == null) {
            return false;
        }
        ARMED.remove(target);
        if (target.body.getWorld() instanceof ServerWorld world) {
            explodeNow(world, target.body, target.owner);
        }
        return true;
    }

    private static Pending findTarget(ServerPlayerEntity player) {
        prune();
        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F).normalize();
        Pending best = null;
        double bestAlignment = 0.0;
        for (Pending pending : ARMED) {
            if (pending.body.getWorld() != player.getWorld()) {
                continue;
            }
            Vec3d to = pending.body.getPos().add(0.0, pending.body.getHeight() * 0.5, 0.0).subtract(eye);
            double length = to.length();
            if (length < 1.0E-4) {
                continue;
            }
            double alignment = to.dotProduct(look) / length;
            if (alignment > bestAlignment) {
                bestAlignment = alignment;
                best = pending;
            }
        }
        return best;
    }

    public static void clear() {
        ARMED.clear();
        SCHEDULED.clear();
    }

    public static void sync(MinecraftServer server) {
        LibertinariArmedPayload payload = payload();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncTo(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, payload());
    }

    private static LibertinariArmedPayload payload() {
        prune();
        List<Integer> ids = new ArrayList<>(ARMED.size());
        for (Pending pending : ARMED) {
            ids.add(pending.body.getId());
        }
        return new LibertinariArmedPayload(ids);
    }

    private static void prune() {
        ARMED.removeIf(pending -> pending.body.isRemoved());
    }

    public static void triggerAround(ServerWorld world, Box box) {
        schedule(world, collect(world, box, null));
    }

    public static void tick(MinecraftServer server) {
        List<Scheduled> due = new ArrayList<>();
        Iterator<Scheduled> iterator = SCHEDULED.iterator();
        while (iterator.hasNext()) {
            Scheduled scheduled = iterator.next();
            if (scheduled.body.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (--scheduled.ticks <= 0) {
                due.add(scheduled);
                iterator.remove();
            }
        }
        for (Scheduled scheduled : due) {
            explodeNow(scheduled.world, scheduled.body, scheduled.owner);
        }
    }

    private static void schedule(ServerWorld world, List<Pending> pending) {
        for (Pending entry : pending) {
            SCHEDULED.add(new Scheduled(world, entry.body, entry.owner, CHAIN_DELAY));
        }
    }

    private static List<Pending> collect(ServerWorld world, Box box, PlayerBodyEntity exclude) {
        List<Pending> found = new ArrayList<>();
        for (Pending pending : ARMED) {
            if (pending.body == exclude || pending.body.isRemoved() || pending.body.getWorld() != world) {
                continue;
            }
            if (box.contains(pending.body.getPos())) {
                found.add(pending);
            }
        }
        ARMED.removeAll(found);
        return found;
    }

    private static void explodeNow(ServerWorld world, PlayerBodyEntity body, UUID owner) {
        Box blast = body.getBoundingBox().expand(3f);
        double x = body.getX();
        double y = body.getY() + EXPLOSION_Y;
        double z = body.getZ();
        world.playSound(null, body.getBlockPos(), WatheSounds.ITEM_GRENADE_EXPLODE, SoundCategory.PLAYERS, 5f, 1f + world.getRandom().nextFloat() * 0.1f - 0.05f);
        world.playSound(null, body.getBlockPos(), SoundEvent.of(Identifier.of("wathedeluxe", "libertinari_explosion")), SoundCategory.PLAYERS, 5f, 1f + world.getRandom().nextFloat() * 0.1f - 0.05f);
        world.spawnParticles(WatheParticles.BIG_EXPLOSION, x, y, z, 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.SMOKE, x, y, z, 100, 0, 0, 0, 0.2);
        ParticleEffect bloodEffect = blood();
        if (bloodEffect != null) {
            world.spawnParticles(bloodEffect, x, y, z, 200, 0.4, 0.4, 0.4, 0.15);
        } else {
            world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState()), x, y, z, 150, 0.4, 0.4, 0.4, 0.15);
        }
        PlayerEntity killer = owner == null ? null : world.getPlayerByUuid(owner);
        for (ServerPlayerEntity player : world.getPlayers(candidate -> blast.contains(candidate.getPos()) && GameFunctions.isPlayerAliveAndSurvival(candidate))) {
            GameFunctions.killPlayer(player, true, killer, GameConstants.DeathReasons.GRENADE);
        }
        body.discard();
        schedule(world, collect(world, blast, body));
    }

    private static ParticleEffect blood() {
        if (!bloodResolved) {
            bloodResolved = true;
            if (FabricLoader.getInstance().isModLoaded("wathe_blood") && Registries.PARTICLE_TYPE.get(Identifier.of("wathe_blood", "blood_particle")) instanceof ParticleEffect effect) {
                blood = effect;
            }
        }
        return blood;
    }
}
