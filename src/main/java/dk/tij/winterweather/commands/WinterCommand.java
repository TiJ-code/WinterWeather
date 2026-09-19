package dk.tij.winterweather.commands;

import dk.tij.winterweather.WinterWeather;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;

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
                        .then(literal("debug")
                                .executes(context -> toggleDebug(context.getSource().getPlayerOrException(), mod))
                                .then(argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    mod.freezeState().setDebug(player.getUUID(), enabled);
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
                                                })))))
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
                ));
    }

    private static int toggleDebug(ServerPlayer player, WinterWeather mod) {
        boolean value = !mod.freezeState().debug(player.getUUID());
        mod.freezeState().setDebug(player.getUUID(), value);
        player.sendSystemMessage(Component.literal("Debug: " + value));
        return 1;
    }
}
