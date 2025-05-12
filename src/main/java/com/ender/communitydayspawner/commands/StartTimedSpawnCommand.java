package com.ender.communitydayspawner.commands;

import com.cobblemon.mod.common.api.permission.CobblemonPermissions;
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;


import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class StartTimedSpawnCommand {

    private static final String NAME = "starttimedspawn";
    private static final String POSITION = "pos";
    private static final String PROPERTIES = "properties";

    private static final SimpleCommandExceptionType INVALID_POS_EXCEPTION =
            new SimpleCommandExceptionType(Text.literal("Invalid position"));
    private static final SimpleCommandExceptionType FAILED_SPAWN_EXCEPTION =
            new SimpleCommandExceptionType(Text.literal("Failed to spawn Pokémon"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(NAME)
                .requires(CobblemonPermissions.SPAWN_POKEMON::check)
                .then(argument(POSITION, Vec3ArgumentType.vec3())
                        .then(argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                                .executes(context -> {
                                    Vec3d pos = Vec3ArgumentType.getVec3(context, POSITION);
                                    PokemonProperties properties = PokemonPropertiesArgumentType.getPokemonProperties(context, PROPERTIES);
                                    return execute(context, pos, properties);
                                }))));
    }
+ private static int execute(CommandContext<ServerCommandSource> context, Vec3d pos, PokemonProperties properties)        ServerWorld world = context.getSource().getWorld();
        BlockPos blockPos = new BlockPos(pos.x, pos.y, pos.z);

        if (!World.isValid(blockPos)) {
            throw INVALID_POS_EXCEPTION.create();
        }

        if (properties.getSpecies() == null) {
            throw new SimpleCommandExceptionType(Text.literal("No species provided")).create();
        }

        PokemonEntity entity = properties.createEntity(world);
        entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, entity.getYaw(), entity.getPitch());
        entity.getDataTracker().set(PokemonEntity.SPAWN_DIRECTION, world.getRandom().nextFloat() * 360F);

        if (world.spawnEntity(entity)) {
            context.getSource().sendFeedback(Text.literal("Spawned Pokémon successfully."), false);
            return Command.SINGLE_SUCCESS;
        }

        throw FAILED_SPAWN_EXCEPTION.create();
    }
}