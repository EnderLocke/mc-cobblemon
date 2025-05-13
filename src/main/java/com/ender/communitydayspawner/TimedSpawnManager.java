package com.ender.communitydayspawner;

import com.ender.communitydayspawner.tracking.CatchTracker;
import com.ender.communitydayspawner.spawners.TimedSpawnInstance;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

// Java standard library
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.Random;
import java.util.Map;
import java.util.Optional;

// Fabric / Minecraft imports
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class TimedSpawnManager {

    private static final List<TimedSpawnInstance> tasks = new ArrayList<>();
    private static final int MIN_LEVEL = 10;
    private static final int LEVEL_RANGE = 31; // max level = MIN_LEVEL + 30
    private static final double SHINY_CHANCE = 0.15;
    private static final int AVERAGE_IV_ROLL = 19;

    public static void activateCommunityDaySpawner(String species, int minutes, BlockPos origin) {
        tasks.add(new TimedSpawnInstance(species, minutes, origin));
    }

    public static void activateLegendaryDaySpawner() {
        tasks.add(new LegendarySpawnInstance());
    }

    public static boolean isCommunityDaySpecies(String species) {
        return tasks.stream().anyMatch(task -> task.getSpecies().equalsIgnoreCase(species));
    }

    public static void tick(ServerWorld world) {
        Iterator<TimedSpawnInstance> iter = tasks.iterator();
        while (iter.hasNext()) {
            TimedSpawnInstance task = iter.next();

            if (task.shouldWarn()) {
                warnCommunityDayEnding(world, task.getSpecies());
                task.markWarned();
            }

            if (task.hasFullyEnded()) {
                endCommunityDay(world, task);
                iter.remove();
            } else {
                task.trySpawn(world);
            }
        }
    }

    private static void warnCommunityDayEnding(ServerWorld world, String species) {
        world.getServer().getPlayerManager().broadcast(
                Text.literal("⚠️ Community Day for ")
                        .append(Text.literal(species).formatted(Formatting.GREEN, Formatting.BOLD))
                        .append(" is ending soon!")
                        .formatted(Formatting.RED),
                false
        );
    }

    private static void endCommunityDay(ServerWorld world, TimedSpawnInstance task) {
        String species = task.getSpecies();
        MinecraftServer server = world.getServer();

        // Notify players
        server.getPlayerManager().broadcast(
                Text.literal("⏰ Community Day for ")
                        .append(Text.literal(species).formatted(Formatting.GREEN, Formatting.BOLD))
                        .append(" has ended!")
                        .formatted(Formatting.YELLOW),
                false
        );

        task.despawnAll(world);

        // Individual stats
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID id = player.getUuid();
            int caught = CatchTracker.getPlayerCaughtCount(id, species);
            int shiny = CatchTracker.getPlayerShinyCount(id, species);

            player.sendMessage(
                    Text.literal("📊 Your stats for ")
                            .append(Text.literal(species).formatted(Formatting.GREEN, Formatting.BOLD))
                            .append(": " + caught + " caught, " + shiny + " shiny")
                            .formatted(Formatting.AQUA),
                    false
            );
        }

        // Top Catchers
        List<Map.Entry<UUID, Integer>> topCaught = CatchTracker.getTopCatchers(species, 3);
        List<Map.Entry<UUID, Integer>> topShiny = CatchTracker.getTopShinyCatchers(species, 3);

        broadcastTopPlayers(server, topCaught, "🏆 Top " + species + " catchers:", Formatting.GOLD);
        broadcastTopPlayers(server, topShiny, "✨ Top shiny hunters:", Formatting.AQUA);

        CatchTracker.reset(); // Clear for next event
    }

    private static void broadcastTopPlayers(MinecraftServer server, List<Map.Entry<UUID, Integer>> entries, String title, Formatting color) {
        server.getPlayerManager().broadcast(Text.literal(""), false);
        server.getPlayerManager().broadcast(Text.literal(title).formatted(color, Formatting.BOLD), false);

        for (int i = 0; i < entries.size(); i++) {
            UUID id = entries.get(i).getKey();
            int count = entries.get(i).getValue();

            String name = Optional.ofNullable(server.getPlayerManager().getPlayer(id))
                    .map(p -> p.getName().getString())
                    .orElse(id.toString());

            server.getPlayerManager().broadcast(
                    Text.literal("  #" + (i + 1) + ": ")
                            .append(Text.literal(name).formatted(Formatting.LIGHT_PURPLE))
                            .append(" - " + count),
                    false
            );
        }
    }

}