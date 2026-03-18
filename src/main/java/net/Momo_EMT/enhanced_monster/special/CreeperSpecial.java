package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperSpecial implements ISpecialElite {
    public static final String TAG_DROP_HEAD = "em_drop_creeper_head";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Creeper creeper)) return;
        
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(creeper.level());
        if (bolt != null) {
            creeper.thunderHit((ServerLevel)creeper.level(), bolt);
        }

        int duration = 144000;
        creeper.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0, false, true));
        creeper.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1, false, true));
        creeper.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 3, false, true));
        creeper.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 3, false, true));

        creeper.getPersistentData().putBoolean(TAG_DROP_HEAD, true);
    }
}