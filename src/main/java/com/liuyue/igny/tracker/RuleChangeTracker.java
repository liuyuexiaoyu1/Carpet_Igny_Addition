package com.liuyue.igny.tracker;

import com.liuyue.igny.data.RuleChangeDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class RuleChangeTracker {

    public static void init(MinecraftServer server){
            RuleChangeDataManager.setServer(server);
    }

    public static void ruleChanged(CommandSourceStack source, carpet.api.settings.CarpetRule<?> rule, String userInput) {
            String sourceName = getSourceName(source);
            long timestamp = System.currentTimeMillis();

            RuleChangeDataManager.recordRuleChange(
                    rule.name(),
                    rule.value(),
                    userInput,
                    sourceName,
                    timestamp
            );
    }

    private static String getSourceName(CommandSourceStack source) {
        try {
            if (source.isPlayer()) {
                return source.getPlayerOrException().getScoreboardName();
            } else if (source.getEntity() != null) {
                return source.getEntity().getScoreboardName();
            } else {
                return source.getTextName();
            }
        } catch (Exception e) {
            return "Console";
        }
    }
}
