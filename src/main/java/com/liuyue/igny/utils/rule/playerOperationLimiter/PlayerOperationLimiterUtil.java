package com.liuyue.igny.utils.rule.playerOperationLimiter;

import carpet.patches.EntityPlayerMPFake;
import com.liuyue.igny.IGNYSettings;
import net.minecraft.server.level.ServerPlayer;
import carpet.fakes.ServerPlayerInterface;

public class PlayerOperationLimiterUtil {

    public static boolean isFakePlayer(ServerPlayer player) {return player instanceof EntityPlayerMPFake;}

    public static boolean canBreakMore(ServerPlayer player, SafeServerPlayerEntity safe) {
        int limit = isFakePlayer(player)
                ? IGNYSettings.fakePlayerBreakLimitPerTick
                : IGNYSettings.realPlayerBreakLimitPerTick;
        return limit == 0 || safe.igny$getBreakCountPerTick() < limit;
    }

    public static boolean canPlaceMore(ServerPlayer player, SafeServerPlayerEntity safe) {
        int limit = isFakePlayer(player)
                ? IGNYSettings.fakePlayerPlaceLimitPerTick
                : IGNYSettings.realPlayerPlaceLimitPerTick;
        return limit == 0 || safe.igny$getPlaceCountPerTick() < limit;
    }
}