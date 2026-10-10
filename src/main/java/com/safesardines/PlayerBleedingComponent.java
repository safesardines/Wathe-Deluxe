package com.safesardines;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

public class PlayerBleedingComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<PlayerBleedingComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of("wathedeluxe", "bleeding"), PlayerBleedingComponent.class);
    public static final Identifier DEATH_REASON = Identifier.of("wathedeluxe", "bleed");
    private static final int DYING_TICKS = 110;
    private final PlayerEntity player;
    public int bleedingTicks = -1;
    public int initialBleedingTicks = 0;
    public int dyingTicks = -1;
    public int shivCount = 0;
    public UUID bleeder;
    public float stabX;
    public float stabY;
    public float stabZ;

    public PlayerBleedingComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        this.bleedingTicks = -1;
        this.initialBleedingTicks = 0;
        this.dyingTicks = -1;
        this.shivCount = 0;
        this.bleeder = null;
        this.stabX = 0.0F;
        this.stabY = 0.0F;
        this.stabZ = 0.0F;
        this.sync();
    }

    public void setBleeding(int ticks, UUID bleeder, float stabX, float stabY, float stabZ) {
        this.bleeder = bleeder;
        if (this.bleedingTicks < 0) {
            this.bleedingTicks = ticks;
            this.initialBleedingTicks = ticks;
        } else {
            this.bleedingTicks = Math.min(this.bleedingTicks, ticks);
            this.initialBleedingTicks = Math.max(this.initialBleedingTicks, ticks);
        }
        this.stabX = stabX;
        this.stabY = stabY;
        this.stabZ = stabZ;
        this.sync();
    }

    @Override
    public void serverTick() {
        if (this.dyingTicks > 0) {
            this.dyingTicks--;
            if (this.dyingTicks <= 0) {
                this.reset();
                return;
            }
            this.sync();
            return;
        }
        if (this.bleedingTicks < 0) {
            return;
        }
        this.bleedingTicks--;
        if (this.bleedingTicks > 0) {
            return;
        }
        this.bleedingTicks = -1;
        this.dyingTicks = DYING_TICKS;
        VoiceDefer.defer(this.player.getUuid());
        PlayerEntity killer = this.bleeder == null ? null : this.player.getWorld().getPlayerByUuid(this.bleeder);
        GameFunctions.killPlayer(this.player, true, killer, DEATH_REASON);
        this.bleeder = null;
        this.shivCount = 0;
        this.sync();
    }

    public static Vec3d rotateY(Vec3d v, float degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3d(v.x * cos + v.z * sin, v.y, -v.x * sin + v.z * cos);
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("bleedingTicks", this.bleedingTicks);
        tag.putInt("initialBleedingTicks", this.initialBleedingTicks);
        tag.putInt("dyingTicks", this.dyingTicks);
        tag.putInt("shivCount", this.shivCount);
        tag.putFloat("stabX", this.stabX);
        tag.putFloat("stabY", this.stabY);
        tag.putFloat("stabZ", this.stabZ);
        if (this.bleeder != null) {
            tag.putUuid("bleeder", this.bleeder);
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.bleedingTicks = tag.contains("bleedingTicks") ? tag.getInt("bleedingTicks") : -1;
        this.initialBleedingTicks = tag.contains("initialBleedingTicks") ? tag.getInt("initialBleedingTicks") : 0;
        this.dyingTicks = tag.contains("dyingTicks") ? tag.getInt("dyingTicks") : -1;
        this.shivCount = tag.contains("shivCount") ? tag.getInt("shivCount") : 0;
        this.stabX = tag.getFloat("stabX");
        this.stabY = tag.getFloat("stabY");
        this.stabZ = tag.getFloat("stabZ");
        this.bleeder = tag.contains("bleeder") ? tag.getUuid("bleeder") : null;
    }
}
