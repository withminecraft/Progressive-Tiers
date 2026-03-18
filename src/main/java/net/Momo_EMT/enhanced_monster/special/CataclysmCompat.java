package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

public class CataclysmCompat {

    public static void spawnFlameStrike(LivingEntity attacker, LivingEntity target, long gameTime) {
        if (!ModList.get().isLoaded("cataclysm")) return;

        long nextReadyTime = attacker.getPersistentData().getLong("em_flame_strike_cooldown");
        if (gameTime < nextReadyTime) return;

        try {
            CataInternal.doSpawn(attacker, target);

            attacker.getPersistentData().putLong("em_flame_strike_cooldown", gameTime + 60);
        } catch (Throwable ignored) {
        }
    }

    private static class CataInternal {
        private static void doSpawn(LivingEntity attacker, LivingEntity target) {
            com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity flameStrike = 
                new com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity(
                    target.level(), 
                    target.getX(), target.getY(), target.getZ(),
                    attacker.getYRot(), 
                    40,    // dur: 持续时间 (40 ticks = 2秒)
                    0,     // delay1
                    10,     // delay2
                    2.0F,  // radius: 半径
                    6.0F,  // dmg: 伤害
                    6.0F,  // 击退
                    true,  
                    attacker // 核心：传入攻击者作为 Owner，灾变内部会处理免伤逻辑
                );
            target.level().addFreshEntity(flameStrike);
        }
    }
}