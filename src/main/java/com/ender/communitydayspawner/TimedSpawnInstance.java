package com.ender.communitydayspawner;

// Java standard library
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.Random;

import com.ender.communitydayspawner.utils.IvUtils;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.StatProvider;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;

// Fabric / Minecraft imports
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;

public class TimedSpawnInstance {
    private final String species;
    private final int minutes;
    private final int durationSeconds;
    private final int totalSeconds;
    private final int warnBeforeEndSeconds;
    private final BlockPos origin;
    private final long startTime;
    private final int minLevel;
    private final int levelRange;
    private final int averageIvRoll;
    private final double shinyChance;
    private final int communityDayChunks;
    private final double spawnChance;
    private final List<UUID> spawnedPokemon = new ArrayList<>();
    private boolean warned = false;

    public TimedSpawnInstance(String species, int minutes, BlockPos origin, int minLevel, int levelRange, int averageIvRoll, double shinyChance, double spawnChance, int communityDayChunks) {
        this.species = species;
        this.minutes = minutes;
        this.origin = origin;
        this.minLevel = minLevel;
        this.spawnChance = spawnChance;
        this.levelRange = levelRange;
        this.averageIvRoll = averageIvRoll;
        this.shinyChance = shinyChance;
        this.communityDayChunks = communityDayChunks;
        this.durationSeconds = minutes * 60;
        this.warnBeforeEndSeconds = 20 + new Random().nextInt(21); // 20–40s
        this.totalSeconds = this.durationSeconds + this.warnBeforeEndSeconds;
        this.startTime = System.currentTimeMillis();
    }

    public TimedSpawnInstance(String species, int minutes, BlockPos origin) {
        this(species, minutes, origin, 10, 31, 19, 0.15, 0.10, 8); // default: minLevel=10, range=31, avgIV=19, shiny=15%
    }

    public PokemonEntity spawnPokemon(ServerWorld world, String species, BlockPos origin) {
        PokemonProperties props = new PokemonProperties();
        props.setSpecies(species);
        props.setLevel(world.getRandom().nextInt(levelRange) + minLevel);
        props.setShiny(Math.random() < shinyChance);

        Pokemon pokemon = props.create();
        if (pokemon == null) {
            System.err.println("❌ Failed to create Pokémon for species: " + species);
            return null;
        }

        StatProvider provider = Cobblemon.INSTANCE.getStatProvider();
        for (Stat stat : provider.ofType(Stat.Type.PERMANENT)) {
            int rolledIv = IvUtils.rollIv(averageIvRoll);
            pokemon.getIvs().set(stat, rolledIv);
        }

        BlockPos spawnPos = getRandomPositionNearby(world, origin);
        PokemonEntity entity = new PokemonEntity(world, pokemon, CobblemonEntities.POKEMON);
        entity.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);

        if (world.spawnEntity(entity)) {
            System.out.println("✅ Spawned " + species + " at " + spawnPos.toShortString());
            return entity;
        } else {
            System.err.println("❌ Failed to spawn " + species + " at " + spawnPos.toShortString());
            return null;
        }
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
        if (!hasSpawningEnded() && world.getRandom().nextFloat() < spawnChance) {
            PokemonEntity entity = spawnPokemon(world, species, origin);
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

    private static BlockPos getRandomPositionNearby(ServerWorld world, BlockPos origin, int chunkRadius) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        int randomChunkX = originChunkX + world.random.nextInt(chunkRadius * 2 + 1) - chunkRadius;
        int randomChunkZ = originChunkZ + world.random.nextInt(chunkRadius * 2 + 1) - chunkRadius;

        int x = (randomChunkX << 4) + world.random.nextInt(16);
        int z = (randomChunkZ << 4) + world.random.nextInt(16);
        int y = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, x, z);

        return new BlockPos(x, y, z);
    }

    private BlockPos getRandomPositionNearby(ServerWorld world, BlockPos origin) {
        return getRandomPositionNearby(world, origin, communityDayChunks);
    }

}