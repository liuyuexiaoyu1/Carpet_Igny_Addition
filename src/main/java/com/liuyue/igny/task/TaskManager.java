package com.liuyue.igny.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TaskManager {
    private static final Map<String, ITask> PLAYER_TASK_MAP = new ConcurrentHashMap<>();

    public static void register(ITask task) {
        String playerName = task.getPlayerName();
        ITask old = PLAYER_TASK_MAP.get(playerName);
        if (old != null && !old.isStopped()) {
            old.stop();
        }
        PLAYER_TASK_MAP.put(playerName, task);
    }

    public static void unregister(ITask task) {
        PLAYER_TASK_MAP.remove(task.getPlayerName(), task);
    }

    public static List<ITask> getAllActiveTasks() {
        return PLAYER_TASK_MAP.values().stream()
                .filter(task -> !task.isStopped())
                .collect(Collectors.toList());
    }

    public static boolean stopTask(String playerName) {
        ITask task = PLAYER_TASK_MAP.get(playerName);
        if (task != null && !task.isStopped()) {
            task.stop();
            return true;
        }
        return false;
    }

    public static boolean pauseTask(String playerName) {
        ITask task = PLAYER_TASK_MAP.get(playerName);
        if (task != null && !task.isStopped() && !task.isPaused()) {
            task.pause();
            return true;
        }
        return false;
    }

    public static boolean resumeTask(String playerName) {
        ITask task = PLAYER_TASK_MAP.get(playerName);
        if (task != null && !task.isStopped() && task.isPaused()) {
            task.resume();
            return true;
        }
        return false;
    }

    public static boolean hasActiveTask(String playerName) {
        ITask task = PLAYER_TASK_MAP.get(playerName);
        return task != null && !task.isStopped();
    }

    public static ITask getTask(String playerName) {
        return PLAYER_TASK_MAP.get(playerName);
    }

    public static void pauseAll() {
        for (ITask task : PLAYER_TASK_MAP.values()) {
            if (!task.isStopped() && !task.isPaused()) {
                task.pause();
            }
        }
    }

    public static void resumeAll() {
        for (ITask task : PLAYER_TASK_MAP.values()) {
            if (!task.isStopped() && task.isPaused()) {
                task.resume();
            }
        }
    }

    public static int getPausedCount() {
        return (int) PLAYER_TASK_MAP.values().stream()
                .filter(task -> !task.isStopped() && task.isPaused())
                .count();
    }

    public static int getRunningCount() {
        return (int) PLAYER_TASK_MAP.values().stream()
                .filter(task -> !task.isStopped() && !task.isPaused())
                .count();
    }
}