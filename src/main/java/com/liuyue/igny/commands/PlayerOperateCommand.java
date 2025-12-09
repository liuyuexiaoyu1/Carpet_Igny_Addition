package com.liuyue.igny.commands;

import carpet.patches.EntityPlayerMPFake;
import com.liuyue.igny.IGNYSettings;
import com.liuyue.igny.task.ITask;
import com.liuyue.igny.task.TaskManager;
import com.liuyue.igny.task.pressuse.PressUseTask;
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
                                Commands.literal("pauseAll")
                                        .executes(PlayerOperateCommand::pauseAllTasks)
                        )
                        .then(
                                Commands.literal("resumeAll")
                                        .executes(PlayerOperateCommand::resumeAllTasks)
                        )
                        .then(
                                Commands.literal("stopAll")
                                        .executes(PlayerOperateCommand::stopAllTasks)
                        )
                        .then(
                                Commands.argument("player", StringArgumentType.string())
                                        .suggests(PlayerOperateCommand::suggestOnlinePlayers)
                                        .then(Commands.literal("task")
                                                .then(
                                                        Commands.literal("vault")
                                                                .executes(ctx -> startVaultTask(ctx, 130))
                                                                .then(
                                                                        Commands.argument("maxCycles", IntegerArgumentType.integer())
                                                                                .executes(PlayerOperateCommand::startVaultTaskWithArg)
                                                                )
                                                )
                                                .then(
                                                        Commands.literal("pressUse")
                                                                .then(
                                                                        Commands.argument("interval", IntegerArgumentType.integer(1))
                                                                                .then(
                                                                                        Commands.argument("duration", IntegerArgumentType.integer(1))
                                                                                                .executes(ctx -> startPressUseTask(ctx, -1))
                                                                                                .then(
                                                                                                        Commands.argument("cycles", IntegerArgumentType.integer(1))
                                                                                                                .executes(ctx -> {
                                                                                                                    int cycles = IntegerArgumentType.getInteger(ctx, "cycles");
                                                                                                                    return startPressUseTask(ctx, cycles);
                                                                                                                })
                                                                                                )
                                                                                )
                                                                )
                                                )
                                        )
                                        .then(
                                                Commands.literal("stop")
                                                        .executes(PlayerOperateCommand::stopTaskForPlayer)
                                        )
                                        .then(
                                                Commands.literal("pause")
                                                        .executes(PlayerOperateCommand::pauseTaskForPlayer)
                                        )
                                        .then(
                                                Commands.literal("resume")
                                                        .executes(PlayerOperateCommand::resumeTaskForPlayer)
                                        )
                        )
                        .then(
                                Commands.literal("list")
                                        .executes(PlayerOperateCommand::listAllTasks)
                        )
        );
    }

    private static int stopAllTasks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<ITask> tasks = TaskManager.getAllActiveTasks();

        if (tasks.isEmpty()) {
            source.sendFailure(Component.literal("§c没有正在运行的任务"));
            return 0;
        }

        int stoppedCount = 0;

        for (ITask task : tasks) {
            if (!task.isStopped()) {
                task.stop();
                stoppedCount++;
            }
        }

        String message;
        if (stoppedCount > 0) {
            message = "§7[PlayerOperate] §c已停止 §f" + stoppedCount + " §c个任务";
        } else {
            message = "§7[PlayerOperate] §c没有可以停止的任务";
        }

        source.sendSuccess(() -> Component.literal(message), false);
        return stoppedCount;
    }

    private static int pauseAllTasks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<ITask> tasks = TaskManager.getAllActiveTasks();

        if (tasks.isEmpty()) {
            source.sendFailure(Component.literal("§c没有正在运行的任务"));
            return 0;
        }

        int pausedCount = 0;
        int alreadyPaused = 0;

        for (ITask task : tasks) {
            if (!task.isPaused()) {
                task.pause();
                pausedCount++;
            } else {
                alreadyPaused++;
            }
        }

        String message;
        if (pausedCount > 0 && alreadyPaused > 0) {
            message = "§7[PlayerOperate] §e已暂停 §f" + pausedCount + " §e个任务, §f" + alreadyPaused + " §e个任务已是暂停状态";
        } else if (pausedCount > 0) {
            message = "§7[PlayerOperate] §e已暂停 §f" + pausedCount + " §e个任务";
        } else {
            message = "§7[PlayerOperate] §e所有任务已是暂停状态";
        }

        source.sendSuccess(() -> Component.literal(message), false);
        return pausedCount;
    }

    private static int resumeAllTasks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<ITask> tasks = TaskManager.getAllActiveTasks();

        if (tasks.isEmpty()) {
            source.sendFailure(Component.literal("§c没有正在运行的任务"));
            return 0;
        }

        int resumedCount = 0;
        int alreadyRunning = 0;

        for (ITask task : tasks) {
            if (task.isPaused()) {
                task.resume();
                resumedCount++;
            } else {
                alreadyRunning++;
            }
        }

        String message;
        if (resumedCount > 0 && alreadyRunning > 0) {
            message = "§7[PlayerOperate] §a已继续 §f" + resumedCount + " §a个任务, §f" + alreadyRunning + " §a个任务已在运行中";
        } else if (resumedCount > 0) {
            message = "§7[PlayerOperate] §a已继续 §f" + resumedCount + " §a个任务";
        } else {
            message = "§7[PlayerOperate] §a所有任务已在运行中";
        }

        source.sendSuccess(() -> Component.literal(message), false);
        return resumedCount;
    }

    private static int pauseTaskForPlayer(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        CommandSourceStack source = context.getSource();

        boolean paused = TaskManager.pauseTask(playerName);
        if (paused) {
            source.sendSuccess(() ->
                            Component.literal("§7[PlayerOperate] §e已暂停 §f" + playerName + " §e的任务"),
                    false
            );
            return 1;
        } else {
            ITask task = TaskManager.getTask(playerName);
            if (task == null || task.isStopped()) {
                source.sendFailure(Component.literal("§c[PlayerOperate] §f" + playerName + " §c没有正在运行的任务"));
            } else if (task.isPaused()) {
                source.sendFailure(Component.literal("§c[PlayerOperate] §f" + playerName + " §c的任务已是暂停状态"));
            }
            return 0;
        }
    }

    private static int resumeTaskForPlayer(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        CommandSourceStack source = context.getSource();

        boolean resumed = TaskManager.resumeTask(playerName);
        if (resumed) {
            source.sendSuccess(() ->
                            Component.literal("§7[PlayerOperate] §a已继续 §f" + playerName + " §a的任务"),
                    false
            );
            return 1;
        } else {
            ITask task = TaskManager.getTask(playerName);
            if (task == null || task.isStopped()) {
                source.sendFailure(Component.literal("§c[PlayerOperate] §f" + playerName + " §c没有正在运行的任务"));
            } else if (!task.isPaused()) {
                source.sendFailure(Component.literal("§c[PlayerOperate] §f" + playerName + " §c的任务已在运行中"));
            }
            return 0;
        }
    }

    private static int startPressUseTask(CommandContext<CommandSourceStack> context, int cycles) {
        String playerName = StringArgumentType.getString(context, "player");
        int interval = IntegerArgumentType.getInteger(context, "interval");
        int duration = IntegerArgumentType.getInteger(context, "duration");
        CommandSourceStack source = context.getSource();

        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            source.sendFailure(Component.literal("§c玩家 §f" + playerName + " §c不在线"));
            return 0;
        }

        if (!(player instanceof EntityPlayerMPFake)) {
            source.sendFailure(Component.literal("§c玩家 §f" + playerName + " §c不是假人"));
            return 0;
        }

        PressUseTask task = PressUseTask.getOrCreate(source, playerName, interval, duration, cycles);
        task.start();

        return 1;
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

        if (!(player instanceof EntityPlayerMPFake)) {
            source.sendFailure(Component.literal("§c玩家 §f" + playerName + " §c不是假人"));
            return 0;
        }

        VaultTask task = VaultTask.getOrCreate(source, playerName, maxCycles);
        task.start();

        return 1;
    }

    private static int stopTaskForPlayer(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        CommandSourceStack source = context.getSource();

        boolean stopped = TaskManager.stopTask(playerName);
        if (stopped) {
            source.sendSuccess(() ->
                            Component.literal("§7[PlayerOperate] §c已停止 §f" + playerName + " §c的任务"),
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

        int runningCount = TaskManager.getRunningCount();
        int pausedCount = TaskManager.getPausedCount();

        Component header = Component.literal("§e正在运行的玩家任务 §7(共 " + tasks.size() + " 个, ")
                .append(Component.literal("§a§l" + runningCount + "§7 运行中, "))
                .append(Component.literal("§e§l" + pausedCount + "§7 暂停)")
                        .append(Component.literal("\n"))
                        .append(createGlobalControlButton("§6[暂停所有]", "/playerOperate pauseAll", "暂停所有任务"))
                        .append(Component.literal(" "))
                        .append(createGlobalControlButton("§a[继续所有]", "/playerOperate resumeAll", "继续所有任务"))
                        .append(Component.literal(" "))
                        .append(createGlobalControlButton("§c[停止所有]","/playerOperate stopAll","停止所有任务"))
                        .append(Component.literal("\n")));

        source.sendSuccess(() -> header, false);

        for (ITask task : tasks) {
            String baseName = task.getPlayerName();
            String taskType = task.getTaskType();
            boolean isPaused = task.isPaused();
            Component pauseButton = createTaskButton("§6[暂停]", "/playerOperate " + baseName + " pause", "暂停此任务", isPaused);
            Component resumeButton = createTaskButton("§a[继续]", "/playerOperate " + baseName + " resume", "继续此任务", !isPaused);
            Component stopButton = createTaskButton("§c[停止]", "/playerOperate " + baseName + " stop", "停止此任务", false);
            String statusIcon = isPaused ? "§8⏸" : "§a▶";
            Component taskInfo = Component.literal(statusIcon + " §f" + baseName)
                    .append(Component.literal(" §6[" + taskType + "] "))
                    .append(task.getStatusText())
                    .append(Component.literal(" "))
                    .append(isPaused ? resumeButton : pauseButton)
                    .append(Component.literal(" "))
                    .append(stopButton);

            source.sendSuccess(() -> taskInfo, false);
        }

        return 1;
    }

    private static Component createTaskButton(String text, String command, String hoverText, boolean disabled) {
        if (disabled) {
            return Component.literal(text.replaceAll("§[0-9a-f]", "§8"))
                    .withStyle(style -> style
                            //#if MC>=12105
                            //$$ .withItalic(true)
                            //$$ .withHoverEvent(new HoverEvent.ShowText(Component.literal("§7" + hoverText + " (不可用)")))
                            //#else
                            .withItalic(true)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§7" + hoverText + " (不可用)")))
                            //#endif
                    );
        } else {
            return Component.literal(text)
                    .withStyle(style -> style
                            //#if MC>=12105
                            //$$ .withClickEvent(new ClickEvent.RunCommand(command))
                            //$$ .withHoverEvent(new HoverEvent.ShowText(Component.literal("§7点击" + hoverText)))
                            //#else
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§7点击" + hoverText)))
                            //#endif
                    );
        }
    }

    private static Component createGlobalControlButton(String text, String command, String hoverText) {
        return Component.literal(text)
                .withStyle(style -> style
                        //#if MC>=12105
                        //$$ .withClickEvent(new ClickEvent.RunCommand(command))
                        //$$ .withHoverEvent(new HoverEvent.ShowText(Component.literal("§7点击" + hoverText)))
                        //#else
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("§7点击" + hoverText)))
                        //#endif
                );
    }
}