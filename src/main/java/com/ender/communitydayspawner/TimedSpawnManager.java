package com.ender.communitydayspawner;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class TimedSpawnManager {

    private static boolean isActive = false;
    private static final List<TimedSpawnInstance> tasks = new ArrayList<>();

    public static void spawnPokemon(ServerWorld world, String species, BlockPos origin) {
        // Build PokemonProperties
        PokemonProperties props = new PokemonProperties();
        props.setSpecies(species);
        int randomLevel = world.getRandom().nextInt(31) + 10; // Generates a number between 10 and 40
        props.setLevel(randomLevel);

        // Increase shiny chance to 15%
        double shinyChance = 0.15; // 15% shiny chance
        if (Math.random() < shinyChance) {
            props.setShiny(true);  // Set the Pokémon as shiny if the random chance is below 0.15
        } else {
            props.setShiny(false); // Otherwise, it's not shiny
        }

        // Create the Pokemon and its entity
        Pokemon pokemon = props.create();
        PokemonEntity entity = new PokemonEntity(world, pokemon, CobblemonEntities.POKEMON);
        BlockPos spawnPos = getRandomPositionInSameChunk(world, origin);
        entity.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);

        // Spawn it in the world with an increased spawn rate
        boolean success = world.spawnEntity(entity);
        if (success) {
            System.out.println("Spawned " + species + " " + origin.toShortString());
        } else {
            System.out.println("Failed to spawn " + species);
        }
    }

    public static void activateSpawner(String species, int minutes, BlockPos origin) {
        tasks.add(new TimedSpawnInstance(species, minutes, origin));
    }

    public static void tick(ServerWorld world) {
        Iterator<TimedSpawnInstance> iter = tasks.iterator();
        while (iter.hasNext()) {
            TimedSpawnInstance task = iter.next();
            if (task.isExpired()) {
                iter.remove();
                System.out.println("Timed spawn ended for " + task.getSpecies());
            } else {
                task.trySpawn(world);
            }
        }
    }

    private static BlockPos getRandomPositionInSameChunk(ServerWorld world, BlockPos origin) {
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;

        int localX = (chunkX << 4) + world.random.nextInt(16);
        int localZ = (chunkZ << 4) + world.random.nextInt(16);

        // Ensure valid y-coordinate
        int y = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, localX, localZ);

        return new BlockPos(localX, y, localZ);
    }
}