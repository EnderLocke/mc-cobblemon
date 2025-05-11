package com.yourmodid.loginstreakmod;

import com.cobblemon.mod.common.api.spawning.SpawnPoolBuilder;
import com.cobblemon.mod.common.api.spawning.detail.*;
import com.cobblemon.mod.common.api.spawning.condition.*;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.PokemonSpec;
import kotlin.Unit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Timer;
import java.util.TimerTask;

public class TimedSpawnManager {

    private static boolean isActive = false;

    public static void startSpawning(String species, int minutes, BlockPos origin) {
        if (isActive) return;
        isActive = true;

        // Create a boosted spawn pool
        SpawnPoolBuilder.register(builder -> {
            builder.species(species);
            builder.rarity(200); // High rarity = higher spawn rate
            builder.shinyChance(0.15); // 5% shiny rate — change as needed
            builder.condition(new BiomeSpawnCondition()); // adjust biome or add time/region conditions
            builder.detail(new SpawnDetail(
                    new PokemonSpec(species),
                    (pokemon) -> Unit.INSTANCE,
                    null
            ));
        });

        // Schedule removal of boosted spawns
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                stopSpawning(species);
            }
        }, minutes * 60 * 1000L);
    }

    public static void stopSpawning(String species) {
        // TODO: Remove the spawn pool programmatically if API supports.
        // Right now Cobblemon does not expose a way to *remove* spawn pools at runtime.
        // As a workaround, you can track it with a flag and ignore it in the condition.

        isActive = false;
        System.out.println("Timed spawn ended for " + species);
    }
}
