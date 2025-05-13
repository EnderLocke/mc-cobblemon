package com.ender.communitydayspawner.commands;

import com.ender.communitydayspawner.TimedSpawnManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class StartLegendarySpawnCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("startlegendaryday")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                            BlockPos pos = ctx.getSource().getPlayer().getBlockPos();

                            // Start legendary spawner
                            TimedSpawnManager.activateLegendaryDaySpawner(minutes, pos);

                            // Feedback to the command sender
                            ctx.getSource().sendFeedback(
                                    () -> Text.literal("✅ You just started a legendary day for " + minutes + " minute(s)."),
                                    false
                            );

                            // Broadcast to all players
                            Text message = Text.literal("🌟 Legendary Day has started! Catch all the ")
                                    .append(Text.literal("legendaries").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD))
                                    .append(" you can in the next ")
                                    .append(Text.literal(String.valueOf(minutes)).formatted(Formatting.YELLOW))
                                    .append(" minute(s)!");

                            ctx.getSource().getServer().getPlayerManager().broadcast(message, false);

                            return 1;
                        })
                )
        );
    }
}