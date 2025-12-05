package com.liuyue.igny.utils;

import com.liuyue.igny.IGNYSettings;
import net.minecraft.commands.CommandSourceStack;
import carpet.patches.EntityPlayerMPFake;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public class CommandPermissions {
    public static boolean canDropEnderChest(CommandSourceStack source, ServerPlayer targetPlayer) {
        if (source == null) {
            return true;
        }

        if (!(targetPlayer instanceof EntityPlayerMPFake)) {
            return source.hasPermission(2);
        }

        String ruleValue = IGNYSettings.commandPlayerEnderChestDrop;

        return switch (ruleValue.toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            case "0" -> source.hasPermission(0);
            case "1" -> source.hasPermission(1);
            case "3" -> source.hasPermission(3);
            case "4" -> source.hasPermission(4);
            default -> source.hasPermission(2);
        };
    }
    public static boolean canUseCommand(CommandSourceStack source, Object commandLevel) {
        if (commandLevel instanceof Boolean) {
            return (Boolean) commandLevel;
        }

        if (commandLevel instanceof String) {
            final String levelStr = ((String) commandLevel).toLowerCase(Locale.ENGLISH);

            switch (levelStr) {
                case "true": return true;
                case "false": return false;
                case "ops": return hasPermissionLevel(source, 2);
            }

            if (levelStr.length() == 1) {
                char c = levelStr.charAt(0);
                if (c >= '0' && c <= '4') {
                    return hasPermissionLevel(source, c - '0');
                }
            }
        }

        return false;
    }

    public static boolean hasPermissionLevel(CommandSourceStack source, int level) {
        return source.hasPermission(level);
    }

    public static boolean canDropEnderChest(CommandSourceStack source) {
        return source == null || source.hasPermission(2);
    }
}