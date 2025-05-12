package com.ender.communitydayspawner.tracking;

import com.ender.communitydayspawner.TimedSpawnManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CatchTracker {

    private static final Map<String, Integer> globalCaughtCounts = new HashMap<>();
    private static final Map<String, Integer> globalShinyCounts = new HashMap<>();

    private static final Map<UUID, Map<String, Integer>> playerCaughtCounts = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> playerShinyCounts = new HashMap<>();

    public static void recordCapture(UUID playerId, String species, boolean isShiny) {
        if (!TimedSpawnManager.isCommunityDaySpecies(species)) return;

        globalCaughtCounts.merge(species, 1, Integer::sum);
        if (isShiny) {
            globalShinyCounts.merge(species, 1, Integer::sum);
        }

        playerCaughtCounts
                .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .merge(species, 1, Integer::sum);

        if (isShiny) {
            playerShinyCounts
                    .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                    .merge(species, 1, Integer::sum);
        }
    }

    public static int getGlobalCaughtCount(String species) {
        return globalCaughtCounts.getOrDefault(species, 0);
    }

    public static int getGlobalShinyCount(String species) {
        return globalShinyCounts.getOrDefault(species, 0);
    }

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

    public static List<Map.Entry<UUID, Integer>> getTopCatchers(String species, int topN) {
        return playerCaughtCounts.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().getOrDefault(species, 0)))
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    public static List<Map.Entry<UUID, Integer>> getTopShinyCatchers(String species, int topN) {
        return playerShinyCounts.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().getOrDefault(species, 0)))
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }
}