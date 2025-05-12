package com.ender.communitydayspawner.commands;

import com.ender.communitydayspawner.TimedSpawnManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.util.math.BlockPos;

public class StartTimedSpawnCommand {
    // Corrected register method signature
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("startcommunityday")
                .requires(source -> source.hasPermissionLevel(2)) // Optional: Permission check
                .then(CommandManager.argument("pokemon", StringArgumentType.word())
                        .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    String pokemon = StringArgumentType.getString(ctx, "pokemon");
                                    int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                                    BlockPos pos = ctx.getSource().getPlayer().getBlockPos();

                                    // Start spawning process
                                    TimedSpawnManager.activateSpawner(pokemon, minutes, pos);

                                    // Send feedback to the player
                                    ctx.getSource().sendFeedback(() ->
                                            Text.literal("✅ You just started the community day for " + pokemon + " started for " + minutes + " minute(s)."), false);
                                    return 1;
                                    //Send to server
                                    ctx.getSource().getServer().getPlayerManager().broadcast(
                                            Text.literal("✅ Community Day has started! Catch all the " + pokemon + " you can for the next " + minutes + " minute(s)."), false);
                                }))));
    }
}