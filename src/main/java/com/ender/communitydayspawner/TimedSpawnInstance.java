package com.ender.communitydayspawner;

// Java standard library
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.Random;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

// Fabric / Minecraft imports
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;

public class TimedSpawnInstance {
    private final String species;
    private final int durationSeconds;
    private final int totalSeconds;
    private final int warnBeforeEndSeconds;
    private final BlockPos origin;
    private final long startTime;

    private final List<UUID> spawnedPokemon = new ArrayList<>();
    private boolean warned = false;

    public TimedSpawnInstance(String species, int minutes, BlockPos origin) {
        this.species = species;
        this.origin = origin;
        this.durationSeconds = minutes * 60;
        this.warnBeforeEndSeconds = 20 + new Random().nextInt(21); // 20–40s
        this.totalSeconds = this.durationSeconds + this.warnBeforeEndSeconds;
        this.startTime = System.currentTimeMillis();
    }

    public String getSpecies() {
        return species;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public boolean hasSpawningEnded() {
        return getElapsedSeconds() > durationSeconds;
    }

    public boolean hasFullyEnded() {
        return getElapsedSeconds() > totalSeconds;
    }

    public boolean shouldWarn() {
        return !warned && getRemainingSeconds() <= warnBeforeEndSeconds;
    }

    public void markWarned() {
        this.warned = true;
    }

    public void trySpawn(ServerWorld world) {
        if (!hasSpawningEnded() && world.getRandom().nextFloat() < 0.10f) {
            PokemonEntity entity = TimedSpawnManager.spawnPokemon(world, species, origin);
            if (entity != null) {
                spawnedPokemon.add(entity.getUuid());
            }
        }
    }

    public void despawnAll(ServerWorld world) {
        for (UUID uuid : spawnedPokemon) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof PokemonEntity) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                System.out.println("Despawned Pokémon with UUID " + uuid);
            }
        }
        spawnedPokemon.clear();
    }

    private long getElapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    private long getRemainingSeconds() {
        return Math.max(0, totalSeconds - getElapsedSeconds());
    }
}