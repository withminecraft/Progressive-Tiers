package net.Momo_EMT.enhanced_monster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EffectPools {
    private static final Random RANDOM = new Random();

    public static class EffectEntry {
        public final String tagName;
        public final int level;

        public EffectEntry(String tagName, int level) {
            this.tagName = tagName;
            this.level = level;
        }
    }

    public static List<EffectEntry> getPool(int quality, boolean isBoss) {
        List<EffectEntry> list = new ArrayList<>();
        
        if (isBoss) {
            addFixed(list, EffectAllocator.PROTECTED, 5); // 保护 VI
            addFixed(list, EffectAllocator.POWERFUL, 5);  // 强力 VI
            addFixed(list, EffectAllocator.REGENERATING, 0);
            addFixed(list, EffectAllocator.STRAY, 2);
            addFixed(list, EffectAllocator.WEAKENER, 2);
            addFixed(list, EffectAllocator.BERSERK, 0);
            addFixed(list, EffectAllocator.LIFESTEAL, 0);
            addFixed(list, EffectAllocator.TANKY, 4);
            addFixed(list, EffectAllocator.VOID, 1);
            return list;
        }

        addFixed(list, EffectAllocator.FIRE_PROT, 0);
        
        if (quality == 1) { // 1级池
            addRand(list, EffectAllocator.PROTECTED, 0, 1); // 保护 I-II
            addRand(list, EffectAllocator.POWERFUL, 0, 1);  // 强力 I-II
            addRand(list, EffectAllocator.SPEEDY, 0, 1);    // 神速 I-II
            addFixed(list, EffectAllocator.POISONOUS, 0);
            addFixed(list, EffectAllocator.STRAY, 0);
            addFixed(list, EffectAllocator.WEAKENER, 0);
            addFixed(list, EffectAllocator.TANKY, 0);
            addFixed(list, EffectAllocator.SUMMONER, 0);
        } else if (quality == 2) { // 2级池
            addRand(list, EffectAllocator.PROTECTED, 2, 3);
            addRand(list, EffectAllocator.POWERFUL, 2, 3);
            addFixed(list, EffectAllocator.REGENERATING, 0);
            addRand(list, EffectAllocator.SPEEDY, 2, 3);
            addRand(list, EffectAllocator.POISONOUS, 0, 1);
            addRand(list, EffectAllocator.STRAY, 0, 1);
            addRand(list, EffectAllocator.WEAKENER, 0, 1);
            addFixed(list, EffectAllocator.BERSERK, 0);
            addFixed(list, EffectAllocator.LIFESTEAL, 0);
            addRand(list, EffectAllocator.TANKY, 1, 2);
            addFixed(list, EffectAllocator.SUMMONER, 0);
        } else if (quality == 3) { // 3级池
            addRand(list, EffectAllocator.PROTECTED, 4, 5);
            addRand(list, EffectAllocator.POWERFUL, 4, 5);
            addFixed(list, EffectAllocator.REGENERATING, 0);
            addRand(list, EffectAllocator.SPEEDY, 4, 5);
            addRand(list, EffectAllocator.POISONOUS, 1, 2);
            addRand(list, EffectAllocator.STRAY, 1, 2);
            addRand(list, EffectAllocator.WEAKENER, 1, 2);
            addFixed(list, EffectAllocator.BERSERK, 0);
            addFixed(list, EffectAllocator.LIFESTEAL, 0);
            addRand(list, EffectAllocator.TANKY, 3, 4);
            addFixed(list, EffectAllocator.VOID, 0);
            addFixed(list, EffectAllocator.SUMMONER, 0);
        }
        return list;
    }

    private static void addRand(List<EffectEntry> l, String tagName, int minLvl, int maxLvl) {
        int level = minLvl + RANDOM.nextInt(maxLvl - minLvl + 1);
        l.add(new EffectEntry(tagName, level));
    }

    private static void addFixed(List<EffectEntry> l, String tagName, int level) {
        l.add(new EffectEntry(tagName, level));
    }
}
