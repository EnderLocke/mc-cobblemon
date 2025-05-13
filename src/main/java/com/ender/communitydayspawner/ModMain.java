package com.ender.communitydayspawner;

import com.ender.communitydayspawner.tracking.CatchTracker;
import com.ender.communitydayspawner.commands.*;
import com.ender.communitydayspawner.boosters.CatchRateBooster;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

import kotlin.Unit;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;

public class ModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register the command with the correct method signature
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartTimedSpawnCommand.register(dispatcher); // Correct method call
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartLegendarySpawnCommand.register(dispatcher); // Correct method call
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartMythicalSpawnCommand.register(dispatcher); // Correct method call
        });

        CatchRateBooster.init();

        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            handlePokemonCapture(event);
            return Unit.INSTANCE;
        });

        // Register tick event for world
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world instanceof ServerWorld serverWorld) {
                TimedSpawnManager.tick(serverWorld);
            }
        });
    }

    private void handlePokemonCapture(PokemonCapturedEvent event) {
        if (event == null || event.getPokemon() == null || event.getPlayer() == null)
            return;

        Pokemon pokemon = event.getPokemon();
        String species = pokemon.getSpecies().getName().toLowerCase();

        boolean isShiny = pokemon.getShiny();

        // Forward the capture to the tracker
        CatchTracker.recordCapture(event.getPlayer().getUuid(), species, isShiny);
    }

}