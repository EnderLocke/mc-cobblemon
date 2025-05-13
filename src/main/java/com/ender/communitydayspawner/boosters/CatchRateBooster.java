package com.ender.communitydayspawner.boosters;

import com.ender.communitydayspawner.TimedSpawnManager;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class CatchRateBooster {
    public static void init() {
        CobblemonEvents.POKEMON_CATCH_RATE.subscribe(Priority.NORMAL, new Function1<PokemonCatchRateEvent, Unit>() {
            @Override
            public Unit invoke(PokemonCatchRateEvent event) {
                String species = event.getPokemonEntity().getPokemon().getSpecies().getName();

                if (TimedSpawnManager.isCommunityDaySpecies(species)) {
                    event.setCatchRate(0.20f); // Set flat 20% catch rate
                    System.out.println("📈 Boosted catch rate to 20% for " + species);
                }

                return Unit.INSTANCE;
            }
        });
    }
}