package com.liuyue.igny.task.vault;

import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import com.liuyue.igny.IGNYServer;
import com.liuyue.igny.IGNYSettings;
import com.liuyue.igny.task.ITask;
import com.liuyue.igny.task.TaskManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VaultTask implements ITask {

    private static final Map<String, VaultTask> INSTANCE_CACHE = new ConcurrentHashMap<>();
    private final MinecraftServer server;
    private final String playerName;
    private final int maxCycles;

    private int currentCycle = 0;
    private int stageTickCounter = 0;
    private int totalTickCounter = 0;
    private ServerPlayer currentFakePlayer = null;
    private Vec3 lastPosition;
    private float lastYaw;
    private float lastPitch;
    private ResourceKey<Level> dimension;
    private boolean isRunning = false;
    private String logoutPlayerName;
    private String pendingFakeName = null;
    private static final int SPAWN_TIMEOUT_TICKS = 400;

    private enum Stage {
        SPAWNING,
        RIGHT_CLICKING,
        WAITING,
        LOGGING_OUT
    }
    private Stage currentStage = Stage.SPAWNING;

    private VaultTask(MinecraftServer server, String playerName, int maxCycles) {
        this.server = server;
        this.playerName = playerName;
        this.maxCycles = Math.max(1, maxCycles);
    }

    public static VaultTask getOrCreate(MinecraftServer server, String playerName, int maxCycles) {
        return INSTANCE_CACHE.computeIfAbsent(playerName, name -> new VaultTask(server, name, maxCycles));
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String getTaskType() {
        return "Vault";
    }

    @Override
    public Component getStatusText() {
        String status = switch (currentStage) {
            case SPAWNING -> "Spawning" + (pendingFakeName != null ? " (" + pendingFakeName + ")" : "");
            case RIGHT_CLICKING -> "Right-clicking";
            case WAITING -> "Waiting";
            case LOGGING_OUT -> "Logging out";
        };
        return Component.literal("§7Cycle §f" + currentCycle + "/" + maxCycles + " §8| §e" + status);
    }

    @Override
    public void stop() {
        if (!isRunning) return;

        isRunning = false;
        cleanupCurrentPlayer();
        pendingFakeName = null;

        currentStage = Stage.SPAWNING;
        stageTickCounter = 0;
        totalTickCounter = 0;
        currentCycle = 0;
        if (INSTANCE_CACHE.values().stream().noneMatch(t -> !t.isStopped())) {
            IGNYSettings.fakePlayerSpawnMemoryLeakFix = false;
        }
        TaskManager.unregister(this);
        INSTANCE_CACHE.remove(playerName);
    }

    @Override
    public boolean isStopped() {
        return !isRunning;
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    public void start() {
        if (isRunning) return;

        ServerPlayer originalPlayer = server.getPlayerList().getPlayerByName(playerName);
        if (originalPlayer == null) {
            broadcastMessage("§c[PlayerOperate] §6Vault§c: 玩家 §f" + playerName + " §c不在线");
            return;
        }

        if (!(originalPlayer instanceof EntityPlayerMPFake)) {
            broadcastMessage("§c[PlayerOperate] §6Vault§c: 玩家 §f" + playerName + " §c不是假人");
            return;
        }
        IGNYSettings.fakePlayerSpawnMemoryLeakFix = true;
        lastPosition = originalPlayer.position();
        lastYaw = originalPlayer.getYRot();
        lastPitch = originalPlayer.getXRot();
        dimension = originalPlayer.level().dimension();

        isRunning = true;
        currentCycle = 0;
        stageTickCounter = 0;
        totalTickCounter = 0;
        currentFakePlayer = originalPlayer;
        startRightClicking();
        currentStage = Stage.RIGHT_CLICKING;
        pendingFakeName = null;

        TaskManager.register(this);

        broadcastMessage("§7[PlayerOperate] §6Vault§7: 已启动任务 §f" + playerName + " §7(maxCycles=" + maxCycles + ")");
    }

    @Override
    public void tick() {
        if (!isRunning) {
            if (currentFakePlayer != null || pendingFakeName != null) {
                cleanupCurrentPlayer();
                pendingFakeName = null;
            }
            return;
        }

        totalTickCounter++;
        stageTickCounter++;

        switch (currentStage) {
            case SPAWNING:
                handleSpawning();
                break;
            case RIGHT_CLICKING:
                handleRightClicking();
                break;
            case WAITING:
                handleWaiting();
                break;
            case LOGGING_OUT:
                handleLoggingOut();
                break;
        }
    }

    private void cleanupCurrentPlayer() {
        if (currentFakePlayer != null) {
            try {
                if (currentFakePlayer instanceof carpet.fakes.ServerPlayerInterface spi) {
                    spi.getActionPack().stopAll();
                }
                if (currentFakePlayer instanceof EntityPlayerMPFake fakePlayer) {
                    fakePlayer.kill(Component.literal("Vault cleanup"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                currentFakePlayer = null;
            }
        }
        pendingFakeName = null;
    }

    private void handleSpawning() {
        if (pendingFakeName == null) {
            cleanupCurrentPlayer();

            currentCycle++;
            if (currentCycle > maxCycles) {
                currentCycle = 1;
            }

            pendingFakeName = playerName + "_" + currentCycle;

            try {
                try {
                    boolean success = EntityPlayerMPFake.createFake(
                            pendingFakeName,
                            server,
                            lastPosition,
                            lastYaw,
                            lastPitch,
                            dimension,
                            GameType.SURVIVAL,
                            false
                    );

                    if (!success) {
                        stop();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
                stop();
            }
        } else {
            ServerPlayer candidate = server.getPlayerList().getPlayerByName(pendingFakeName);
            if (candidate != null && candidate.isAlive()) {
                currentFakePlayer = candidate;
                pendingFakeName = null;
                startRightClicking();
            }
        }
    }

    private void startRightClicking() {
        if (currentFakePlayer != null && currentFakePlayer.isAlive()) {
            try {
                if (currentFakePlayer instanceof carpet.fakes.ServerPlayerInterface spi) {
                    EntityPlayerActionPack actionPack = spi.getActionPack();
                    actionPack.stopAll();
                    actionPack.start(EntityPlayerActionPack.ActionType.USE,
                            EntityPlayerActionPack.Action.continuous());

                    currentStage = Stage.RIGHT_CLICKING;
                    stageTickCounter = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentStage = Stage.LOGGING_OUT;
                stageTickCounter = 0;
            }
        } else {
            currentStage = Stage.SPAWNING;
            stageTickCounter = 0;
            pendingFakeName = null;
        }
    }

    private void handleRightClicking() {
        if (currentFakePlayer == null || !currentFakePlayer.isAlive()) {
            currentStage = Stage.SPAWNING;
            stageTickCounter = 0;
            pendingFakeName = null;
            return;
        }

        if (stageTickCounter >= 100) {
            currentStage = Stage.LOGGING_OUT;
            stageTickCounter = 0;
        }
    }

    private void handleLoggingOut() {
        if (currentFakePlayer != null) {
            logoutPlayerName = currentFakePlayer.getGameProfile()
                    //#if MC>=12109
                    //$$ .name();
                    //#else
                    .getName();
                    //#endif

            if (currentFakePlayer instanceof carpet.fakes.ServerPlayerInterface spi) {
                spi.getActionPack().start(EntityPlayerActionPack.ActionType.USE, null);
            }
            if (currentFakePlayer instanceof EntityPlayerMPFake fakePlayer) {
                fakePlayer.kill(Component.literal("Vault cycle completed"));
            }
            currentFakePlayer = null;
        }

        currentStage = Stage.WAITING;
        stageTickCounter = 0;
    }

    private void handleWaiting() {
        if (stageTickCounter >= 21) {
            currentStage = Stage.SPAWNING;
            stageTickCounter = 0;
            pendingFakeName = null;
        }
    }

    private void broadcastMessage(String message) {
        server.getPlayerList().getPlayers().forEach(player -> {
            if (player.hasPermissions(2)) {
                player.sendSystemMessage(Component.literal(message));
            }
        });
        IGNYServer.LOGGER.info(message);
    }

    public String getPendingFakeName() { return pendingFakeName; }
    public ServerPlayer getCurrentFakePlayer() { return currentFakePlayer; }
    public String getLogoutPlayerName() { return logoutPlayerName; }


}