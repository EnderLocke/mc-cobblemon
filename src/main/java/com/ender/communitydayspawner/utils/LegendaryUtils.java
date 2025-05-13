package com.ender.communitydayspawner.utils;

import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;

import java.util.List;
import java.util.stream.Collectors;

public class LegendaryUtils {
    public static List<String> getAllLegendarySpecies() {
        return PokemonSpecies.INSTANCE.getSpecies().stream()
                .filter(species ->
                        species.getForms().stream()
                                .flatMap(form -> form.getLabels().stream())
                                .anyMatch(label -> label.equalsIgnoreCase("legendary"))
                )
                .map(Species::getName)
                .collect(Collectors.toList());
    }
}