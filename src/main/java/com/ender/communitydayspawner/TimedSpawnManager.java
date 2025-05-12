package com.ender.communitydayspawner;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Timer;
import java.util.TimerTask;

public class TimedSpawnManager {

    private static boolean isActive = false;

    public static void startSpawning(ServerWorld world, String species, int minutes, BlockPos origin) {
        //if (isActive) return;
        //isActive = true;

        // Build PokemonProperties
        PokemonProperties props = new PokemonProperties();
        props.setSpecies(species);
        props.setLevel(10); // You can randomize this if desired

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
            System.out.println("Spawned " + species + " (Shiny: " + props.isShiny() + ") at " + origin.toShortString());
        } else {
            System.out.println("Failed to spawn " + species);
            isActive = false;
            return;
        }

        // Schedule removal
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                stopSpawning(species);
            }
        }, minutes * 60 * 1000L);
    }

    public static void stopSpawning(String species) {
        isActive = false;
        System.out.println("Timed spawn ended for " + species);
    }

    private static BlockPos getRandomPositionInSameChunk(ServerWorld world, BlockPos origin) {
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;

        int localX = (chunkX << 4) + world.random.nextInt(16);
        int localZ = (chunkZ << 4) + world.random.nextInt(16);

        int y = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, localX, localZ);

        return new BlockPos(localX, y, localZ);
    }

}
