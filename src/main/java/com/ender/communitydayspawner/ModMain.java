package com.ender.communitydayspawner;

import com.ender.communitydayspawner.commands.StartTimedSpawnCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

public class ModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register the command with the correct method signature
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartTimedSpawnCommand.register(dispatcher); // Correct method call
        });

        // Register tick event for world
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world instanceof ServerWorld serverWorld) {
                TimedSpawnManager.tick(serverWorld);
            }
        });
    }
}