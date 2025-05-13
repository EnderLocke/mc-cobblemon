package com.ender.communitydayspawner;

import com.ender.communitydayspawner.tracking.CatchTracker;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class TimedSpawnManager {

    private static final List<TimedSpawnInstance> tasks = new ArrayList<>();
    private static final int MIN_LEVEL = 10;
    private static final int LEVEL_RANGE = 31; // max level = MIN_LEVEL + 30
    private static final double SHINY_CHANCE = 0.15;

    public static void activateSpawner(String species, int minutes, BlockPos origin) {
        tasks.add(new TimedSpawnInstance(species, minutes, origin));
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

            if (task.isOver()) {
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

    public static PokemonEntity spawnPokemon(ServerWorld world, String species, BlockPos origin) {
        PokemonProperties props = new PokemonProperties();
        props.setSpecies(species);
        props.setLevel(world.getRandom().nextInt(LEVEL_RANGE) + MIN_LEVEL);
        props.setShiny(Math.random() < SHINY_CHANCE);

        Pokemon pokemon = props.create();
        if (pokemon == null) {
            System.err.println("❌ Failed to create Pokémon for species: " + species);
            return null;
        }

        BlockPos spawnPos = getRandomPositionNearby(world, origin);
        PokemonEntity entity = new PokemonEntity(world, pokemon, CobblemonEntities.POKEMON);
        entity.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);

        if (world.spawnEntity(entity)) {
            System.out.println("✅ Spawned " + species + " at " + spawnPos.toShortString());
            return entity;
        } else {
            System.err.println("❌ Failed to spawn " + species + " at " + spawnPos.toShortString());
            return null;
        }
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

    private static BlockPos getRandomPositionNearby(ServerWorld world, BlockPos origin) {
        return getRandomPositionNearby(world, origin, 10);
    }

    private static BlockPos getRandomPositionNearby(ServerWorld world, BlockPos origin, int chunkRadius) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        int randomChunkX = originChunkX + world.random.nextInt(chunkRadius * 2 + 1) - chunkRadius;
        int randomChunkZ = originChunkZ + world.random.nextInt(chunkRadius * 2 + 1) - chunkRadius;

        int x = (randomChunkX << 4) + world.random.nextInt(16);
        int z = (randomChunkZ << 4) + world.random.nextInt(16);
        int y = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, x, z);

        return new BlockPos(x, y, z);
    }
}