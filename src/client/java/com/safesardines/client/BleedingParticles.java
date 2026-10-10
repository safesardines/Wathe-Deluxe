package com.safesardines.client;

import com.safesardines.PlayerBleedingComponent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BleedingParticles {
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static boolean resolved;
    private static ParticleEffect blood;

    private BleedingParticles() {
    }

    private static final class State {
        int remaining;
        int total;
        Vec3d stab;
    }

    public static void tick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) {
            STATES.clear();
            return;
        }
        Set<UUID> seen = new HashSet<>();
        for (PlayerEntity player : world.getPlayers()) {
            PlayerBleedingComponent component = PlayerBleedingComponent.KEY.get(player);
            if (component.bleedingTicks <= 0) {
                continue;
            }
            UUID id = player.getUuid();
            seen.add(id);
            State state = STATES.get(id);
            if (state == null) {
                state = new State();
                state.total = Math.max(1, component.initialBleedingTicks);
                state.remaining = component.bleedingTicks;
                state.stab = new Vec3d(component.stabX, component.stabY, component.stabZ);
                STATES.put(id, state);
            }
            state.remaining--;
            float progress = 1.0F - (float) Math.max(0, state.remaining) / state.total;
            spawn(player, state.stab, Math.max(0.0F, Math.min(1.0F, progress)));
        }
        STATES.keySet().retainAll(seen);
    }

    private static void spawn(PlayerEntity player, Vec3d local, float progress) {
        ParticleEffect effect = blood();
        if (effect == null) {
            return;
        }
        Vec3d offset = PlayerBleedingComponent.rotateY(local, player.getYaw());
        double x = player.getX() + offset.x;
        double y = player.getY() + offset.y;
        double z = player.getZ() + offset.z;
        if (player.getRandom().nextFloat() >= 0.2F + 0.35F * progress) {
            return;
        }
        double vx = (player.getRandom().nextDouble() - 0.5) * 0.2;
        double vy = (player.getRandom().nextDouble() - 0.5) * 0.2;
        double vz = (player.getRandom().nextDouble() - 0.5) * 0.2;
        player.getWorld().addParticle(effect, x, y, z, vx, vy, vz);
    }

    private static ParticleEffect blood() {
        if (!resolved) {
            resolved = true;
            if (FabricLoader.getInstance().isModLoaded("wathe_blood") && Registries.PARTICLE_TYPE.get(Identifier.of("wathe_blood", "blood_particle")) instanceof ParticleEffect effect) {
                blood = effect;
            }
        }
        return blood;
    }
}
