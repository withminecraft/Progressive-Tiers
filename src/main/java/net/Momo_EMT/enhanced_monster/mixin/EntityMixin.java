package net.Momo_EMT.enhanced_monster.mixin;

import net.Momo_EMT.enhanced_monster.client.EMClientGlowingHandler;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(at = @At("HEAD"), method = "isCurrentlyGlowing", cancellable = true)
    public void em$isGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity)(Object)this;
        if (entity.level().isClientSide && EMClientGlowingHandler.isCustomGlow(entity)) {
            cir.setReturnValue(true);
        }
    }
}