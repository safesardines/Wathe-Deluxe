package com.safesardines.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.safesardines.PlayerBleedingComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BleedingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("wathedeluxe:bleeding")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("victim", EntityArgumentType.player())
                                .then(CommandManager.argument("bleeder", EntityArgumentType.player())
                                        .then(CommandManager.argument("duration", IntegerArgumentType.integer(1))
                                                .executes(context -> bleed(
                                                        EntityArgumentType.getPlayer(context, "victim"),
                                                        EntityArgumentType.getPlayer(context, "bleeder"),
                                                        IntegerArgumentType.getInteger(context, "duration"))))))
        );
    }

    private static int bleed(ServerPlayerEntity victim, ServerPlayerEntity bleeder, int seconds) {
        PlayerBleedingComponent.KEY.get(victim).setBleeding(GameConstants.getInTicks(0, seconds), bleeder.getUuid(), 0.0F, 0.9F, 0.0F);
        return 1;
    }
}
