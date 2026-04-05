package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;

public class SpiderSpecial implements ISpecialElite {
    public static final String TAG_WEB_ATTACK = "em_spider_web_attack";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Spider spider)) return;

        spider.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 144000, 0, false, true));
        spider.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 144000, 1, false, true));

        spider.getPersistentData().putBoolean(TAG_WEB_ATTACK, true);
    }
}