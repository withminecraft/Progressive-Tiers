package net.Momo_EMT.enhanced_monster.client;

import net.Momo_EMT.enhanced_monster.EffectAllocator;
import net.minecraft.world.entity.Entity;

public class EMClientGlowingHandler {

    public static boolean isCustomGlow(Entity entity) {
        return entity.getPersistentData().contains(EffectAllocator.TAG_QUALITY);
    }
}