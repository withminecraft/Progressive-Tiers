package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.HashMap;
import java.util.Map;

public class SpecialManager {
    private static final Map<String, ISpecialElite> HANDLERS = new HashMap<>();

    static {
        HANDLERS.put("minecraft:zombie", new ZombieSpecial());
        HANDLERS.put("minecraft:skeleton", new SkeletonSpecial());
        HANDLERS.put("minecraft:creeper", new CreeperSpecial());
        HANDLERS.put("minecraft:vindicator", new VindicatorSpecial());
        HANDLERS.put("minecraft:evoker", new EvokerSpecial());
        HANDLERS.put("minecraft:piglin_brute", new PiglinBruteSpecial());
        HANDLERS.put("minecraft:drowned", new DrownedSpecial());
        HANDLERS.put("minecraft:wither_skeleton", new WitherSkeletonSpecial());
        HANDLERS.put("minecraft:piglin", new PiglinSpecial());
        HANDLERS.put("minecraft:zombified_piglin", new ZombifiedPiglinSpecial());
        HANDLERS.put("minecraft:pillager", new PillagerSpecial());
    }

    public static void tryApply(LivingEntity entity, int quality) {
        if (quality < 3) return;

        ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (rl != null) {
            ISpecialElite handler = HANDLERS.get(rl.toString());
            if (handler != null) {
                handler.apply(entity);
            }
        }
    }
}