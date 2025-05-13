package com.ender.communitydayspawner.commands;


package com.ender.communitydayspawner.commands;

import com.ender.communitydayspawner.TimedSpawnManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.util.math.BlockPos;

public class StartLegendarySpawnCommand {
    // Corrected register method signature
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("startlegendaryday")
                .requires(source -> source.hasPermissionLevel(2)) // Optional: Permission check
                    .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                                BlockPos pos = ctx.getSource().getPlayer().getBlockPos();

                                // Start spawning process
                                TimedSpawnManager.activateLegendaryDaySpawner(minutes, pos);

                                // Send feedback to the command executor
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("✅ You just started a legendary day  for " + minutes + " minute(s)."), false);

                                // Broadcast to all players
                                Text message = Text.literal("✅ Legendary Day has started! Catch all the ")
                                        .append("Legendaries you can for the next ")
                                        .append(Text.literal(String.valueOf(minutes)).formatted(Formatting.YELLOW))
                                        .append(" minute(s).");

                                ctx.getSource().getServer().getPlayerManager().broadcast(message, false);

                                return 1;
                            })
                    )
                ));
    }
}