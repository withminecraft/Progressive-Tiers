package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EffectAllocator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TraitConfig {
    private static final Map<String, Integer> ITEM_ENABLED_TRAITS = new HashMap<>();

    static {
        ITEM_ENABLED_TRAITS.put(EffectAllocator.POWERFUL, 5); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.TANKY, 4); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.PROTECTED, 5);  
        ITEM_ENABLED_TRAITS.put(EffectAllocator.SPEEDY, 5); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.FIRE_PROT, 0); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.POISONOUS, 2); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.STRAY, 2); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.WEAKENER, 2); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.BERSERK, 0); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.LIFESTEAL, 0); 
        ITEM_ENABLED_TRAITS.put(EffectAllocator.WITHERING, 2);
    }

    public static boolean hasItem(String traitType) {
        return ITEM_ENABLED_TRAITS.containsKey(traitType);
    }

    public static int getMaxLevel(String traitType) {
        return ITEM_ENABLED_TRAITS.getOrDefault(traitType, 0);
    }

    public static Set<String> getValidTraits() {
        return ITEM_ENABLED_TRAITS.keySet();
    }
}