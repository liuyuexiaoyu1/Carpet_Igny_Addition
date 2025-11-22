package com.liuyue.igny.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.liuyue.igny.IGNYServerMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RuleChangeDataManager {
    private static final String DATA_FILE_NAME = "rule_changes.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Map<String, RuleChangeRecord> changeHistory = new HashMap<>();
    private static MinecraftServer server;
    private static Path dataFilePath;

    public static void setServer(MinecraftServer server) {
        RuleChangeDataManager.server = server;
        if (server != null) {
            dataFilePath = server.getWorldPath(LevelResource.ROOT).resolve(IGNYServerMod.getModId()).resolve(DATA_FILE_NAME);
            loadFromFile();
        }
    }

    public static void recordRuleChange(String ruleName, Object originalValue,
                                        String userInput, String sourceName, long timestamp) {

            RuleChangeRecord record = new RuleChangeRecord(
                    ruleName, originalValue, userInput, sourceName, timestamp
            );
            changeHistory.put(ruleName, record);
            saveToFile();

    }

    public static Optional<RuleChangeRecord> getLastChange(String ruleName) {
        return Optional.ofNullable(changeHistory.get(ruleName));
    }

    public static Map<String, RuleChangeRecord> getAllChanges() {
        return new HashMap<>(changeHistory);
    }

    public static void clearHistory(String ruleName) {
        if (changeHistory.remove(ruleName) != null) {
            saveToFile();
        }
    }

    public static void clearAllHistory() {
        changeHistory.clear();
        saveToFile();
    }

    private static void loadFromFile() {
        if (dataFilePath == null) {
            return;
        }

        try {
            // 如果文件不存在，创建空的 JSON 文件
            if (!Files.exists(dataFilePath)) {
                Files.createDirectories(dataFilePath.getParent());
                Files.writeString(dataFilePath, "{}");
                changeHistory.clear();
                return;
            }

            // 读取现有文件
            String json = Files.readString(dataFilePath);

            // 如果文件为空，初始化为空 Map
            if (json == null || json.trim().isEmpty()) {
                changeHistory.clear();
                return;
            }

            Type type = new TypeToken<Map<String, RuleChangeRecord>>(){}.getType();
            Map<String, RuleChangeRecord> loaded = GSON.fromJson(json, type);

            if (loaded != null) {
                changeHistory.clear();
                changeHistory.putAll(loaded);
            } else {
                System.err.println("规则变更记录文件格式错误");
            }
        } catch (IOException e) {
            System.err.println("加载规则变更记录文件失败: " + e.getMessage());
        }
    }

    private static void saveToFile() {
        if (dataFilePath == null) {
            return;
        }

        try {
            if (!Files.exists(dataFilePath.getParent())) {
                Files.createDirectories(dataFilePath.getParent());
            }

            String json = GSON.toJson(changeHistory);
            Files.writeString(dataFilePath, json);
        } catch (IOException e) {
            System.err.println("保存规则变更记录文件失败: " + e.getMessage());
        }
    }

    public static class RuleChangeRecord {
        public final String ruleName;
        public final Object rawValue;
        public final Object currentValue;
        public final String sourceName;
        public final long timestamp;
        public final String formattedTime;

        public RuleChangeRecord(String ruleName, Object rawValue, Object currentValue,String sourceName, long timestamp) {
            this.ruleName = ruleName;
            this.rawValue = rawValue;
            this.currentValue = currentValue;
            this.sourceName = sourceName;
            this.timestamp = timestamp;
            this.formattedTime = formatTimestamp(timestamp);
        }

        private String formatTimestamp(long timestamp) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date(timestamp));
        }

        // 检查记录是否有效（没有 null 值）
        public boolean isValid() {
            return ruleName != null && !ruleName.isEmpty() &&
                    rawValue != null &&
                    currentValue != null &&
                    sourceName != null && !sourceName.isEmpty() &&
                    formattedTime != null && !formattedTime.isEmpty();
        }

        // GSON 需要无参构造函数用于反序列化
        @SuppressWarnings("unused")
        private RuleChangeRecord() {
            this.ruleName = "";
            this.rawValue = null;
            this.currentValue = null;
            this.sourceName = "";
            this.timestamp = 0;
            this.formattedTime = "";
        }
    }
}