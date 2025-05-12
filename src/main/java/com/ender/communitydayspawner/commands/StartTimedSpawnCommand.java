package com.ender.communitydayspawner.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import com.ender.communitydayspawner.TimedSpawnManager;  // Ensure this import matches the package where TimedSpawnManager is located

public class StartTimedSpawnCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("starttimedspawn")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("pokemon", StringArgumentType.word())
                            .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                                    .executes(ctx -> {
                                        String pokemon = StringArgumentType.getString(ctx, "pokemon");
                                        int minutes = IntegerArgumentType.getInteger(ctx, "minutes");

                                        // Get the player's position and world
                                        ServerCommandSource source = ctx.getSource();
                                        BlockPos pos = source.getPlayer().getBlockPos();
                                        ServerWorld world = source.getWorld();

                                        // Start spawning the Pokémon
                                        TimedSpawnManager.startSpawning(world, pokemon, minutes, pos);

                                        // Send feedback to the player
                                        source.sendFeedback(() ->
                                                Text.literal("✅ Timed spawn of " + pokemon + " started for " + minutes + " minute(s)."), false);

                                        return 1;
                                    }))));
        });
    }
}