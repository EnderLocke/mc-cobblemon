package com.ender.communitydayspawner;

import com.ender.communitydayspawner.commands.StartTimedSpawnCommand;  // Adjust to your correct package path

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.math.BlockPos;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;


public class ModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(ModMain::registerCommands);

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!(world instanceof ServerWorld serverWorld)) return;

            // Example: 1% chance to spawn per tick (~once every 5 seconds per world)
            if (serverWorld.getRandom().nextFloat() < 0.01F) {
                BlockPos playerPos = serverWorld.getPlayers().isEmpty()
                        ? serverWorld.getSpawnPos()
                        : serverWorld.getPlayers().get(0).getBlockPos();

                TimedSpawnManager.startSpawning(serverWorld, "pikachu", 2, playerPos);
            }
        });
    });
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        StartTimedSpawnCommand.register(); // Pass the dispatcher
        //StopTimedSpawnCommand.register(dispatcher);  // Pass the dispatcher
    }
}
