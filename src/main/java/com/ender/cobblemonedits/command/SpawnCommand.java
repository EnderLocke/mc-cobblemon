package com.ender.cobblemonedits.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SpawnCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("starttimedspawn")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("pokemon", StringArgumentType.word())
                            .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                                    .executes(ctx -> {
                                        String pokemon = StringArgumentType.getString(ctx, "pokemon");
                                        int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                                        BlockPos pos = ctx.getSource().getPlayer().getBlockPos();

                                        TimedSpawnManager.startSpawning(pokemon, minutes, pos);
                                        ctx.getSource().sendFeedback(() ->
                                                Text.literal("✅ Timed spawn of " + pokemon + " started for " + minutes + " minute(s)."), false);
                                        return 1;
                                    }))));
        });
    }
}