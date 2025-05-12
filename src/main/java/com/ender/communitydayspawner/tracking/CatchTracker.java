package com.ender.communitydayspawner.tracking;

import com.cobblemon.mod.common.api.events.pokemon.PokemonCaughtCallback;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CatchTracker {

    // Global counts
    private static final Map<String, Integer> globalCaughtCounts = new HashMap<>();
    private static final Map<String, Integer> globalShinyCounts = new HashMap<>();

    // Per-player counts
    private static final Map<UUID, Map<String, Integer>> playerCaughtCounts = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> playerShinyCounts = new HashMap<>();

    public static void init() {
        PokemonCaughtCallback.EVENT.register((player, pokemon, method) -> {
            String species = pokemon.getSpecies().getName();
            UUID playerId = player.getUuid();

            if (TimedSpawnManager.isCommunityDaySpecies(species)) {
                // Global counters
                globalCaughtCounts.merge(species, 1, Integer::sum);
                if (pokemon.isShiny()) {
                    globalShinyCounts.merge(species, 1, Integer::sum);
                }

                // Per-player counters
                playerCaughtCounts.computeIfAbsent(playerId, k -> new HashMap<>())
                        .merge(species, 1, Integer::sum);

                if (pokemon.isShiny()) {
                    playerShinyCounts.computeIfAbsent(playerId, k -> new HashMap<>())
                            .merge(species, 1, Integer::sum);
                }

                // Debug output
                System.out.println("🎉 " + player.getName().getString() + " caught " +
                        (pokemon.isShiny() ? "a shiny " : "a ") + species + "!");
            }

            return null; // Don't override vanilla behavior
        });
    }

    // Global accessors
    public static int getGlobalCaughtCount(String species) {
        return globalCaughtCounts.getOrDefault(species, 0);
    }

    public static int getGlobalShinyCount(String species) {
        return globalShinyCounts.getOrDefault(species, 0);
    }

    // Per-player accessors
    public static int getPlayerCaughtCount(UUID playerId, String species) {
        return playerCaughtCounts.getOrDefault(playerId, new HashMap<>())
                .getOrDefault(species, 0);
    }

    public static int getPlayerShinyCount(UUID playerId, String species) {
        return playerShinyCounts.getOrDefault(playerId, new HashMap<>())
                .getOrDefault(species, 0);
    }

    public static void reset() {
        globalCaughtCounts.clear();
        globalShinyCounts.clear();
        playerCaughtCounts.clear();
        playerShinyCounts.clear();
    }
}