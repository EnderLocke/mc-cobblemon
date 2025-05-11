package com.ender.cobblemonedits;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.math.BlockPos;

public class ModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        SpawnCommand.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getWorlds().forEach(world -> {
                TimedSpawnManager.onServerTick((ServerWorld) world);
            });
        });
    }
}
