package com.liuyue.igny.mixins.compat.fapi;

import carpet.patches.FakeClientConnection;
import com.liuyue.igny.IGNYSettings;
import com.liuyue.igny.mixins.compat.accessor.fapi.AbstractChanneledNetworkAddonAccessor;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.AbstractNetworkAddon;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = AbstractNetworkAddon.class, priority = 999, remap = false)
public abstract class AbstractNetworkAddonMixin {
    @SuppressWarnings("RedundantIfStatement")
    @WrapWithCondition(method = "lateInit", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/networking/GlobalReceiverRegistry;startSession(Lnet/fabricmc/fabric/impl/networking/AbstractNetworkAddon;)V"))
    private boolean notStartSession_ifFakeClientConnection(GlobalReceiverRegistry<?> instance, AbstractNetworkAddon<?> addon) {
        if ((Boolean.TRUE.equals(getCarpetOrgAdditionSetting())||IGNYSettings.fakePlayerSpawnMemoryLeakFix) && addon instanceof AbstractChanneledNetworkAddon<?>) {
            Connection connection = ((AbstractChanneledNetworkAddonAccessor) addon).getConnection();
            if (connection instanceof FakeClientConnection) {
                return false;
            }
        }
        return true;
    }
    @Unique
    private static Boolean getCarpetOrgAdditionSetting() {
        try {
            Class<?> settingsClass = Class.forName("org.carpetorgaddition.CarpetOrgAdditionSettings");

            Field field = settingsClass.getField("fakePlayerSpawnMemoryLeakFix");
            Object supplierObj = field.get(null);
            if (supplierObj instanceof java.util.function.Supplier<?>) {
                Object result = ((java.util.function.Supplier<?>) supplierObj).get();
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
            }
            return false;

        } catch (ClassNotFoundException ignored) {
            return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
