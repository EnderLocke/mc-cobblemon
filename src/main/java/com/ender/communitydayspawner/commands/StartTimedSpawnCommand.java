package com.ender.communitydayspawner.commands;

import com.ender.communitydayspawner.TimedSpawnManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Timer;
import java.util.TimerTask;

public class StartTimedSpawnCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("starttimedspawn")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("pokemon", StringArgumentType.word())
                            .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                                    .executes(ctx -> {
                                        String pokemon = StringArgumentType.getString(ctx, "pokemon");
                                        int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                                        BlockPos pos = ctx.getSource().getPlayer().getBlockPos();

                                        // Start spawning process
                                        TimedSpawnManager.activateSpawner(pokemon, minutes, pos);

                                        ctx.getSource().sendFeedback(() ->
                                                Text.literal("✅ Timed spawn of " + pokemon + " started for " + minutes + " minute(s)."), false);
                                        return 1;
                                    }))));
        });
    }
}
