package com.liuyue.igny.mixins.rule.fakePlayerLoginLogoutNoInfo;

import carpet.patches.EntityPlayerMPFake;
import com.liuyue.igny.IGNYSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerList.class,priority = 1500)
public class PlayerListMixin {
    @WrapOperation(method="placeNewPlayer",at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void broadcastSystemMessage(PlayerList instance, Component component, boolean bl, Operation<Void> original,@Local(argsOnly = true) ServerPlayer serverPlayer) {
        if (serverPlayer instanceof EntityPlayerMPFake && IGNYSettings.fakePlayerLoginLogoutNoChatInfo) {
            return;
        }
        original.call(instance, component, bl);
    }
}
