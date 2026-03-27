package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Evoker;

public class EvokerSpecial implements ISpecialElite {
    public static final String TAG_TOTEM_COUNT = "em_totem_resurrections"; 
    public static final String TAG_DROP_TOTEM = "em_drop_extra_totem";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Evoker evoker)) return;
        
        evoker.getPersistentData().putInt(TAG_TOTEM_COUNT, 5);
        
        evoker.getPersistentData().putBoolean(TAG_DROP_TOTEM, true);
    }
}