package com.ender.communitydayspawner;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class TimedSpawnManager {

    private static boolean isActive = false;
    private static final List<TimedSpawnInstance> tasks = new ArrayList<>();

    public static PokemonEntity spawnPokemon(ServerWorld world, String species, BlockPos origin) {
        // Build properties
        PokemonProperties props = new PokemonProperties();
        props.setSpecies(species);

        int level = world.getRandom().nextInt(31) + 10; // Level 10–40
        props.setLevel(level);

        // 15% shiny chance
        props.setShiny(Math.random() < 0.15);

        // Create the Pokémon instance
        Pokemon pokemon = props.create();
        if (pokemon == null) {
            System.err.println("❌ Failed to create Pokémon for species: " + species);
            return null;
        }

        // Create the entity
        PokemonEntity entity = new PokemonEntity(world, pokemon, CobblemonEntities.POKEMON);
        BlockPos spawnPos = getRandomPositionNearby(world, origin);
        entity.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);

        // Try to spawn it
        boolean success = world.spawnEntity(entity);
        if (success) {
            System.out.println("✅ Spawned " + species + " at " + spawnPos.toShortString());
            return entity;
        } else {
            System.err.println("❌ Failed to spawn " + species + " at " + spawnPos.toShortString());
            return null;
        }
    }

    public static void activateSpawner(String species, int minutes, BlockPos origin) {
        tasks.add(new TimedSpawnInstance(species, minutes, origin));
    }

    public static void tick(ServerWorld world) {
        Iterator<TimedSpawnInstance> iter = tasks.iterator();
        while (iter.hasNext()) {
            TimedSpawnInstance task = iter.next();
            if (task.isExpired()) {
                String species = task.getSpecies();
                world.getServer().getPlayerManager().broadcast(
                        Text.literal("⏰ Community Day for " + species + " has ended!"), false
                );
                task.despawnAll(world);

                MinecraftServer server = world.getServer();

                // Send per-player stats
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    UUID id = player.getUuid();
                    int caught = CatchTracker.getPlayerCaughtCount(id, species);
                    int shiny = CatchTracker.getPlayerShinyCount(id, species);

                    player.sendMessage(Text.literal("📊 Your stats for " + species + ": " +
                            caught + " caught, " + shiny + " shiny"), false);
                }

                // Broadcast top 3
                List<Map.Entry<UUID, Integer>> topCaught = CatchTracker.getTopCatchers(species, 3);
                List<Map.Entry<UUID, Integer>> topShiny = CatchTracker.getTopShinyCatchers(species, 3);

                server.getPlayerManager().broadcast(Text.literal("🏆 Top " + species + " catchers:"), false);
                for (int i = 0; i < topCaught.size(); i++) {
                    String name = server.getPlayerManager().getPlayer(topCaught.get(i).getKey()) != null ?
                            server.getPlayerManager().getPlayer(topCaught.get(i).getKey()).getName().getString() :
                            topCaught.get(i).getKey().toString();
                    int count = topCaught.get(i).getValue();
                    server.getPlayerManager().broadcast(Text.literal("  #" + (i+1) + ": " + name + " - " + count), false);
                }

                server.getPlayerManager().broadcast(Text.literal("✨ Top shiny hunters:"), false);
                for (int i = 0; i < topShiny.size(); i++) {
                    String name = server.getPlayerManager().getPlayer(topShiny.get(i).getKey()) != null ?
                            server.getPlayerManager().getPlayer(topShiny.get(i).getKey()).getName().getString() :
                            topShiny.get(i).getKey().toString();
                    int count = topShiny.get(i).getValue();
                    server.getPlayerManager().broadcast(Text.literal("  #" + (i+1) + ": " + name + " - " + count), false);
                }

                // Optional: clear counts for the next event
                CatchTracker.reset();
                iter.remove();
            } else {
                task.trySpawn(world);
            }
        }
    }

    private static BlockPos getRandomPositionNearby(ServerWorld world, BlockPos origin) {
        return getRandomPositionNearby(world, origin, 10);
    }

    private static BlockPos getRandomPositionNearby(ServerWorld world, BlockPos origin, int chunkRadius) {
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;

        // Pick a random chunk within the chunkRadius range around the origin chunk
        int randomChunkX = originChunkX + world.random.nextInt(chunkRadius * 2 + 1) - chunkRadius;
        int randomChunkZ = originChunkZ + world.random.nextInt(chunkRadius * 2 + 1) - chunkRadius;

        // Then pick a random block position within that chunk
        int localX = (randomChunkX << 4) + world.random.nextInt(16);
        int localZ = (randomChunkZ << 4) + world.random.nextInt(16);

        int y = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, localX, localZ);

        return new BlockPos(localX, y, localZ);
    }
}