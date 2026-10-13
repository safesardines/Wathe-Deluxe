package com.safesardines.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PoisonCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("wathedeluxe:poison")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("victim", EntityArgumentType.player())
                                .then(CommandManager.argument("poisoner", EntityArgumentType.player())
                                        .then(CommandManager.argument("duration", IntegerArgumentType.integer(1))
                                                .executes(context -> poison(
                                                        EntityArgumentType.getPlayer(context, "victim"),
                                                        EntityArgumentType.getPlayer(context, "poisoner"),
                                                        IntegerArgumentType.getInteger(context, "duration"))))))
        );
    }

    private static int poison(ServerPlayerEntity victim, ServerPlayerEntity poisoner, int seconds) {
        PlayerPoisonComponent.KEY.get(victim).setPoisonTicks(GameConstants.getInTicks(0, seconds), poisoner.getUuid());
        return 1;
    }
}
