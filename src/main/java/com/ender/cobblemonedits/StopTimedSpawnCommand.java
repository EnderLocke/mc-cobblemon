package com.ender.cobblemonedits;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.yourmodid.loginstreakmod.TimedSpawnManager;

public class StopTimedSpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stoptimedspawn")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(StopTimedSpawnCommand::run));
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TimedSpawnManager.stopSpawning("pikachu"); // or make this dynamic
        context.getSource().sendFeedback(() -> Text.literal("⛔ Timed spawn stopped."), true);
        return 1;
    }
}