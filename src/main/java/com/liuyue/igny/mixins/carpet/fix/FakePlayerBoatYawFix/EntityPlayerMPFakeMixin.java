package com.liuyue.igny.mixins.carpet.fix.FakePlayerBoatYawFix;

import carpet.patches.EntityPlayerMPFake;
import com.liuyue.igny.IGNYSettings;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeMixin {
    @Inject(method = "tick",at= @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;tick()V"))
    private void tick(CallbackInfo ci) {
        if (IGNYSettings.fakePlayerBoatYawFix) {
            EntityPlayerMPFake fakePlayer = (EntityPlayerMPFake) (Object) this;
            if (fakePlayer.getVehicle() instanceof Boat boat) {
                fakePlayer.setYRot(boat.getYRot());
                fakePlayer.yHeadRot = fakePlayer.getYRot();
                fakePlayer.serverLevel().getChunkSource().broadcastAndSend(fakePlayer, new ClientboundRotateHeadPacket(fakePlayer, (byte) (fakePlayer.yHeadRot * 256 / 360)));
            }
        }
    }
}
