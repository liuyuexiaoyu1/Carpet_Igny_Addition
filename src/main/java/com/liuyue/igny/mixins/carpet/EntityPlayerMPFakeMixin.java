package com.liuyue.igny.mixins.carpet;

import carpet.patches.EntityPlayerMPFake;
import com.liuyue.igny.task.ITask;
import com.liuyue.igny.task.TaskManager;
import com.liuyue.igny.task.vault.VaultTask;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//#if MC>=12111
//$$ import java.util.UUID;
//$$ import net.minecraft.server.MinecraftServer;
//#else
import java.util.Optional;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.GameProfileCache;
//#endif

@Mixin(EntityPlayerMPFake.class)
public abstract class EntityPlayerMPFakeMixin {

    @WrapOperation(
            method = "createFake",
            at = @At(value = "INVOKE",
                    //#if MC >= 12111
                    //$$ target = "Lnet/minecraft/server/players/OldUsersConverter;convertMobOwnerIfNecessary(Lnet/minecraft/server/MinecraftServer;Ljava/lang/String;)Ljava/util/UUID;"
                    //#else
                    target = "Lnet/minecraft/server/players/GameProfileCache;get(Ljava/lang/String;)Ljava/util/Optional;"
                    //#endif
            )
    )
    //#if MC >= 12111
    //$$ private static UUID getFakeUUID(MinecraftServer minecraftServer, String string, Operation<UUID> original, @Local(argsOnly = true, name = "arg0") String username) {
    //#else
    private static Optional<GameProfile> getFakeProfile(GameProfileCache instance, String string, Operation<Optional<GameProfile>> original, @Local(argsOnly = true, name = "arg0") String username) {
    //#endif
        //#if MC >= 12111
        //$$ UUID uuid = original.call(minecraftServer, string);
        //#else
        Optional<GameProfile> gameProfile = original.call(instance, string);
        //#endif
        //#if MC >= 12111
        //$$ if(uuid == null) {
        //#else
        if(gameProfile.isEmpty()) {
        //#endif
            for (ITask task : TaskManager.getAllActiveTasks()) {
                if (task instanceof VaultTask vaultTask && username.equals(vaultTask.getPendingFakeName())) {
                    //#if MC >= 12111
                    //$$ return UUIDUtil.createOfflinePlayerUUID(username);
                    //#else
                    return Optional.of(new GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username));
                    //#endif
                }
            }
        }
        //#if MC >= 12111
        //$$ return uuid;
        //#else
        return gameProfile;
        //#endif
    }
}