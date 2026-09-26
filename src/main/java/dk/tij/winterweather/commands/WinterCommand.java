package dk.tij.winterweather.commands;

import dk.tij.winterweather.WinterWeather;
import dk.tij.winterweather.heat.HeatSourceManager;
import dk.tij.winterweather.network.FreezeNetworking;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class WinterCommand {
    private WinterCommand() {}

    public static void register(WinterWeather mod) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("winter")
                        .requires(source -> source.permissions() instanceof LevelBasedPermissionSet permissions
                                && permissions.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS))
                        .then(literal("start").executes(context -> {
                            mod.setEnabled(true);
                            context.getSource().sendSuccess(() -> Component.literal("Winter has started."), true);
                            return 1;
                        }))
                        .then(literal("stop").executes(context -> {
                            mod.setEnabled(false);
                            context.getSource().sendSuccess(() -> Component.literal("Winter has stopped."), true);
                            return 1;
                        }))
                        .then(literal("reload").executes(context -> {
                            mod.reload();
                            context.getSource().sendSuccess(() -> Component.literal("Configuration reloaded."), true);
                            return 1;
                        }))
                        .then(literal("lock")
                                .executes(context -> setLockedLookingAt(context, true))
                                .then(argument("pos", BlockPosArgument.blockPos()).executes(context ->
                                        setLocked(
                                                context.getSource(),
                                                BlockPosArgument.getBlockPos(context, "pos"),
                                                true
                                        ))))
                        .then(literal("unlock")
                                .executes(context -> setLockedLookingAt(context, false))
                                .then(argument("pos", BlockPosArgument.blockPos()).executes(context ->
                                        setLocked(
                                                context.getSource(),
                                                BlockPosArgument.getBlockPos(context, "pos"),
                                                false
                                        ))))
                        .then(literal("particle")
                                .then(literal("on")
                                        .executes(context -> setCampfireSmoke(context, false, null))
                                        .then(argument("pos", BlockPosArgument.blockPos()).executes(context ->
                                                setCampfireSmoke(context, false, BlockPosArgument.getBlockPos(context, "pos")))))
                                .then(literal("off")
                                        .executes(context -> setCampfireSmoke(context, true, null))
                                        .then(argument("pos", BlockPosArgument.blockPos()).executes(context ->
                                                setCampfireSmoke(context, true, BlockPosArgument.getBlockPos(context, "pos"))))))
                        .then(literal("debug")
                                .executes(context -> toggleDebug(context.getSource().getPlayerOrException(), mod))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    mod.freezeState().setDebug(player.getUUID(), enabled);
                                    FreezeNetworking.sendDebugState(player, enabled);
                                    context.getSource().sendSuccess(() -> Component.literal("Debug: " + enabled), false);
                                    return 1;
                                })))
                        .then(literal("config")
                                .then(literal("reload").executes(context -> {
                                    mod.reload();
                                    context.getSource().sendSuccess(() -> Component.literal("Configuration reloaded."), true);
                                    return 1;
                                }))
                                .then(literal("set")
                                        .then(argument("path", StringArgumentType.word())
                                                .then(argument("value", StringArgumentType.greedyString()).executes(context -> {
                                                    String path = StringArgumentType.getString(context, "path");
                                                    String value = StringArgumentType.getString(context, "value");
                                                    if (!mod.setConfigValue(path, value)) {
                                                        context.getSource().sendFailure(Component.literal("Path not found or value is invalid."));
                                                        return 0;
                                                    }
                                                    mod.reload();
                                                    context.getSource().sendSuccess(() -> Component.literal("Configuration updated."), true);
                                                    return 1;
                                                }))))
                                .then(literal("get")
                                        .then(argument("path", StringArgumentType.word()).executes(context -> {
                                            String path = StringArgumentType.getString(context, "path");
                                            String value = mod.configValue(path);
                                            if (value == null) {
                                                context.getSource().sendFailure(Component.literal("Path not found."));
                                                return 0;
                                            }
                                            context.getSource().sendSuccess(() -> Component.literal(path + " = " + value), false);
                                            return 1;
                                        })))
                        )
                ));
    }

    private static int setLockedLookingAt(CommandContext<CommandSourceStack> context, boolean locked)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            context.getSource().sendFailure(Component.literal("Look at an extinguishable heat source block."));
            return 0;
        }
        return setLocked(context.getSource(), blockHit.getBlockPos(), locked);
    }

    private static int setLocked(CommandSourceStack source, BlockPos pos, boolean locked) {
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("This command can only be used in-game."));
            return 0;
        }

        HeatSourceManager manager = WinterWeather.heatSourceManagerOrNull();
        if (manager == null) {
            source.sendFailure(Component.literal("Heat source manager is not available."));
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        if (!manager.isExtinguishable(state)) {
            source.sendFailure(Component.literal("That block is not an extinguishable heat source."));
            return 0;
        }

        if (!manager.setLocked(level, pos, locked)) {
            source.sendFailure(Component.literal("Could not update lock state."));
            return 0;
        }

        String action = locked ? "Locked" : "Unlocked";
        source.sendSuccess(
                () -> Component.literal(action + " heat source at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                true
        );
        return 1;
    }

    private static int setCampfireSmoke(CommandContext<CommandSourceStack> context, boolean suppressed, BlockPos target) {
        CommandSourceStack source = context.getSource();
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("This command can only be used in-game."));
            return 0;
        }
        if (target == null) {
            ServerPlayer player;
            try {
                player = source.getPlayerOrException();
            } catch (CommandSyntaxException exception) {
                source.sendFailure(Component.literal("Provide a campfire position when using this command from the console."));
                return 0;
            }
            HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
            if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
                source.sendFailure(Component.literal("Look at a campfire."));
                return 0;
            }
            target = blockHit.getBlockPos();
        }

        HeatSourceManager manager = WinterWeather.heatSourceManagerOrNull();
        if (manager == null || !manager.setSmokeSuppressed(level, target, suppressed)) {
            source.sendFailure(Component.literal("That block is not a campfire."));
            return 0;
        }
        final BlockPos pos = target;
        for (ServerPlayer player : level.players()) {
            FreezeNetworking.sendCampfireSmoke(player, pos, suppressed);
        }
        source.sendSuccess(() -> Component.literal("Campfire smoke particles "
                + (suppressed ? "disabled" : "enabled") + " at "
                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);
        return 1;
    }

    private static int toggleDebug(ServerPlayer player, WinterWeather mod) {
        boolean value = !mod.freezeState().debug(player.getUUID());
        mod.freezeState().setDebug(player.getUUID(), value);
        FreezeNetworking.sendDebugState(player, value);
        player.sendSystemMessage(Component.literal("Debug: " + value));
        return 1;
    }
}
