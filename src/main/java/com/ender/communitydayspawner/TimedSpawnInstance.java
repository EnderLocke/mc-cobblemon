package com.ender.communitydayspawner;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;

public class TimedSpawnInstance {
    private final String species;
    private final int minutes;
    private final BlockPos origin;
    private final long startTime;

    public TimedSpawnInstance(String species, int minutes, BlockPos origin) {
        this.species = species;
        this.minutes = minutes;
        this.origin = origin;  // Track the origin of the spawn
        this.startTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed > (minutes * 60 * 1000L);
    }

    public void trySpawn(ServerWorld world) {
        if (world.getRandom().nextFloat() < 0.25f) { // 25% chance per tick
            TimedSpawnManager.spawnPokemon(world, species, origin);  // Use the stored origin for spawn
        }
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public String getSpecies() {
        return species;
    }

}