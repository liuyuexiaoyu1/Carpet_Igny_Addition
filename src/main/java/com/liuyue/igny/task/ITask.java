package com.liuyue.igny.task;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public interface ITask {
    String getPlayerName();
    String getTaskType();
    Component getStatusText();
    void start();
    void stop();
    boolean isStopped();
    boolean isPaused();
    void pause();
    void resume();
    MinecraftServer getServer();
    void tick();
}