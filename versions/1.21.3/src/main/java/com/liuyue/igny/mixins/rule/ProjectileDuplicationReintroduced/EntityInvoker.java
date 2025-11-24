package com.liuyue.igny.mixins.rule.ProjectileDuplicationReintroduced;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Set;

@Mixin(Entity.class)
public interface EntityInvoker {

    @Invoker("applyGravity")
    void invokeApplyGravity();

    //#if MC>=12105
    //$$ @Invoker("applyEffectsFromBlocks")
    //$$ void invokeApplyEffectsFromBlocks();
    //#endif

}
