package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CaveSpider;

public class CaveSpiderSpecial implements ISpecialElite {
    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof CaveSpider caveSpider)) return;

        caveSpider.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 144000, 0, false, true));

        caveSpider.getPersistentData().putBoolean(SpiderSpecial.TAG_WEB_ATTACK, true);
    }
}