package com.ender.communitydayspawner.boosters;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;
import com.ender.communitydayspawner.TimedSpawnManager;

public class CatchRateBooster {
    public static void init() {
        CobblemonEvents.POKEMON_CATCH_RATE.subscribe(event -> {
            String species = event.getPokemonEntity().getPokemon().getSpecies().getName();

            if (TimedSpawnManager.isCommunityDaySpecies(species)) {
                float originalRate = event.getCatchRate();
                event.setCatchRate(0.20f); // Flat 20% catch rate
                System.out.println("Boosted catch rate for " + species + ": " + originalRate + " → " + event.getCatchRate());
            }

            return null; // Required due to functional interface compatibility
        });
    }
}