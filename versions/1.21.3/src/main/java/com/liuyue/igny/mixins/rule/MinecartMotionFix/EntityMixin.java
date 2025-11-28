package com.liuyue.igny.mixins.rule.MinecartMotionFix;

import com.liuyue.igny.IGNYSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "calculatePassengerTransition",at = @At("RETURN"), cancellable = true)
    private void getPassengerTeleportTarget(TeleportTransition teleportTarget, Entity passenger,
                                            CallbackInfoReturnable<TeleportTransition> cir){
        if(IGNYSettings.MinecartMotionFix) {
            cir.setReturnValue(new TeleportTransition(teleportTarget.newLevel
                    (), teleportTarget.position(), teleportTarget.deltaMovement(),
                    0, 0, teleportTarget.postTeleportTransition()));
        }
    }
}
