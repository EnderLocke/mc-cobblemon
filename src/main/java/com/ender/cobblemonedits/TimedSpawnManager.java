package com.ender.cobblemonedits;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.factory.PokemonFactory;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Timer;
import java.util.TimerTask;

public class TimedSpawnManager {

    private static boolean isActive = false;

    public static void startSpawning(ServerWorld world, String species, int minutes, BlockPos origin) {
        if (isActive) return;
        isActive = true;

        // Build Pokémon
        Pokemon pokemon = PokemonFactory.INSTANCE.create(species);

        // Create the in-world entity
        PokemonEntity entity = new PokemonEntity(world, pokemon);
        entity.refreshPositionAndAngles(origin, 0.0F, 0.0F);

        // Spawn it
        world.spawnEntity(entity);

        System.out.println("Spawned " + species + " at " + origin.toShortString());

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
}