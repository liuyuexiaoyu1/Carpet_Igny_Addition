package com.liuyue.igny.commands;

import com.liuyue.igny.IGNYSettings;
import com.liuyue.igny.task.ITask;
import com.liuyue.igny.task.TaskManager;
import com.liuyue.igny.task.vault.VaultTask;
import com.liuyue.igny.utils.CommandPermissions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerOperateCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("playerOperate")
                        .requires(source -> CommandPermissions.canUseCommand(source, IGNYSettings.commandPlayerOperate))
                        .then(
                                Commands.argument("player", StringArgumentType.string())
                                        .suggests(PlayerOperateCommand::suggestOnlinePlayers)
                                        .then(
                                                Commands.literal("vault")
                                                        .executes(ctx -> startVaultTask(ctx, 130))
                                                        .then(
                                                                Commands.argument("maxCycles", IntegerArgumentType.integer())
                                                                        .executes(PlayerOperateCommand::startVaultTaskWithArg)
                                                        )
                                        )
                                        .then(
                                                Commands.literal("stop")
                                                        .executes(PlayerOperateCommand::stopTaskForPlayer)
                                        )
                        )
                        .then(
                                Commands.literal("list")
                                        .executes(PlayerOperateCommand::listAllTasks)
                        )
        );
    }

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        CommandSourceStack source = context.getSource();
        return SharedSuggestionProvider.suggest(
                source.getServer().getPlayerList().getPlayers().stream()
                        .map(ServerPlayer::getName)
                        .map(Component::getString),
                builder
        );
    }

    private static int startVaultTaskWithArg(CommandContext<CommandSourceStack> context) {
        int maxCycles = IntegerArgumentType.getInteger(context, "maxCycles");
        return startVaultTask(context, maxCycles);
    }

    private static int startVaultTask(CommandContext<CommandSourceStack> context, int maxCycles) {
        String playerName = StringArgumentType.getString(context, "player");
        CommandSourceStack source = context.getSource();

        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.literal("§c玩家 §f" + playerName + " §c不在线"));
            return 0;
        }

        if (!(player instanceof carpet.patches.EntityPlayerMPFake)) {
            source.sendFailure(Component.literal("§c玩家 §f" + playerName + " §c不是假人"));
            return 0;
        }

        VaultTask task = VaultTask.getOrCreate(source.getServer(), playerName, maxCycles);
        task.start();

        return 1;
    }

    private static int stopTaskForPlayer(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        CommandSourceStack source = context.getSource();

        boolean stopped = TaskManager.stopTask(playerName);
        if (stopped) {
            source.sendSuccess(() ->
                            Component.literal("§7[PlayerOperate] 已停止 §f" + playerName + " §7的任务"),
                    false
            );
            return 1;
        } else {
            source.sendFailure(Component.literal("§c[PlayerOperate] §f" + playerName + " §c没有正在运行的任务"));
            return 0;
        }
    }

    private static int listAllTasks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<ITask> tasks = TaskManager.getAllActiveTasks();

        if (tasks.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7没有正在运行的玩家任务"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§e正在运行的玩家任务 §7(共 " + tasks.size() + " 个):"), false);

        for (ITask task : tasks) {
            String baseName = task.getPlayerName();
            String taskType = task.getTaskType();

            String stopCmd = "/playerOperate " + baseName + " stop";

            Component stopButton = Component.literal("§c[停止]")
                    .withStyle(style -> style
                                    //#if MC>=12105
                                    //$$ .withClickEvent(new ClickEvent.RunCommand(stopCmd))
                                    //$$ .withHoverEvent(new HoverEvent.ShowText(Component.literal("§7点击停止任务")))
                                    //#else
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, stopCmd))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§7点击停止任务")))
                                    //#endif
                    );

            Component taskInfo = Component.literal("§f" + baseName)
                    .append(Component.literal(" §6[" + taskType + "] "))
                    .append(task.getStatusText())
                    .append(Component.literal(" "))
                    .append(stopButton);

            source.sendSuccess(() -> taskInfo, false);
        }

        return 1;
    }
}