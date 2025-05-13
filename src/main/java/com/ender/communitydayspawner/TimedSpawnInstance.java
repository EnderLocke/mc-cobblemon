package com.ender.communitydayspawner;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TimedSpawnInstance {
    private final String species;
    private final int minutes;
    private final BlockPos origin;
    private final long startTime;
    private final List<UUID> spawnedPokemon = new ArrayList<>();
    private final int warnTimeRemainingSeconds;
    private int cDaySeconds;
    private boolean warned = false;
    private int cdTotalSeconds;

    public TimedSpawnInstance(String species, int minutes, BlockPos origin) {
        this.species = species;
        this.minutes = minutes;
        this.origin = origin;
        this.startTime = System.currentTimeMillis();
        this.warnTimeRemainingSeconds = 20 + new Random().nextInt(21); // 20–40 seconds
        this.cDaySeconds = minutes * 60; // seconds
        this.cdTotalSeconds = this.warnTimeRemainingSeconds + this.cDaySeconds;
    }

    public boolean isExpired() {
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed > (cDaySeconds);
    }
    
    public boolean isOver() {
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed > (cDaySeconds);
    }

    public void trySpawn(ServerWorld world) {
        if (world.getRandom().nextFloat() < 0.10f) {
            PokemonEntity entity = TimedSpawnManager.spawnPokemon(world, species, origin);
            if (entity != null) {
                spawnedPokemon.add(entity.getUuid());
            }
        }
    }

    public void despawnAll(ServerWorld world) {
        for (UUID uuid : spawnedPokemon) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof PokemonEntity pokemon) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                System.out.println("Despawned " + pokemon.getDisplayName().getString() +
                        " at " + pokemon.getBlockPos());
            }
        }
        spawnedPokemon.clear();
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public String getSpecies() {
        return species;
    }

    public boolean shouldWarn() {
        return !warned && isExpired();
    }

    public void markWarned() {
        this.warned = true;
    }
}