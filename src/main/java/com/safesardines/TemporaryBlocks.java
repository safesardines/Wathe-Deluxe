package com.safesardines;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TemporaryBlocks {
    public static final TagKey<Block> UNBREAKABLE = TagKey.of(RegistryKeys.BLOCK, Identifier.of("wathedeluxe", "unbreakable"));
    public static final TagKey<Block> FRAGILE = TagKey.of(RegistryKeys.BLOCK, Identifier.of("wathedeluxe", "fragile"));

    public record Snapshot(BlockState state, NbtCompound blockEntityNbt) {
    }

    private static final Map<ServerWorld, Map<BlockPos, Snapshot>> snapshots = new HashMap<>();
    private static final Map<ServerWorld, Set<BlockPos>> placed = new HashMap<>();

    public static void breakTemporary(ServerWorld world, BlockPos pos) {
        if (!WathedeluxeConfig.HANDLER.instance().destruction) {
            return;
        }
        temporaryBreak(world, pos, false, true);
    }

    public static void temporaryBreak(ServerWorld world, BlockPos pos, boolean drop, boolean protectTagged) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir() || (protectTagged && state.isIn(UNBREAKABLE)) || state.getHardness(world, pos) < 0.0F) {
            return;
        }
        snapshot(world, pos, state);
        world.breakBlock(pos, drop, null, 512);
    }

    public static void temporaryPlace(ServerWorld world, BlockPos pos) {
        snapshot(world, pos, world.getBlockState(pos));
        placed.computeIfAbsent(world, w -> new HashSet<>()).add(pos.toImmutable());
    }

    private static void snapshot(ServerWorld world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        snapshots.computeIfAbsent(world, w -> new HashMap<>()).putIfAbsent(pos.toImmutable(),
                new Snapshot(state, blockEntity == null ? null : blockEntity.createNbt(world.getRegistryManager())));
    }

    public static void snapshotBlock(ServerWorld world, BlockPos pos) {
        if (!WathedeluxeConfig.HANDLER.instance().placement) {
            return;
        }
        snapshot(world, pos, world.getBlockState(pos));
    }

    public static void trackPlaced(ServerWorld world, BlockPos pos) {
        if (!WathedeluxeConfig.HANDLER.instance().placement) {
            return;
        }
        placed.computeIfAbsent(world, w -> new HashSet<>()).add(pos.toImmutable());
    }

    public static void sweepWorld(ServerWorld world) {
        Set<BlockPos> restored = new HashSet<>();
        Map<BlockPos, Snapshot> worldSnaps = snapshots.get(world);
        if (worldSnaps != null) {
            List<BlockPos> order = new ArrayList<>(worldSnaps.keySet());
            order.sort(Comparator.comparingInt(BlockPos::getY));
            for (BlockPos pos : order) {
                Snapshot snap = worldSnaps.get(pos);
                world.setBlockState(pos, snap.state(), Block.NOTIFY_ALL);
                if (snap.blockEntityNbt() != null) {
                    BlockEntity restoredBe = world.getBlockEntity(pos);
                    if (restoredBe != null) {
                        restoredBe.read(snap.blockEntityNbt(), world.getRegistryManager());
                    }
                }
                restored.add(pos);
            }
            worldSnaps.clear();
        }
        Set<BlockPos> worldPlaced = placed.get(world);
        if (worldPlaced != null) {
            for (BlockPos pos : worldPlaced) {
                if (!restored.contains(pos)) {
                    world.removeBlock(pos, false);
                }
            }
            worldPlaced.clear();
        }
    }
}
