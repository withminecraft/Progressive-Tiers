package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;

public class WitchSpecial implements ISpecialElite {
    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Witch witch)) return;

        witch.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 144000, 1, false, true));
        witch.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 144000, 1, false, true));
    }
}