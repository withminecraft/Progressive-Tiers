package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;

public class CataclysmCompat {

    public static void spawnFlameStrike(LivingEntity attacker, LivingEntity target, long gameTime) {
        if (!ModList.get().isLoaded("cataclysm")) return;

        long nextReadyTime = attacker.getPersistentData().getLong("em_flame_strike_cooldown");
        if (gameTime < nextReadyTime) return;

        try {
            CataInternal.doSpawn(attacker, target);

            attacker.getPersistentData().putLong("em_flame_strike_cooldown", gameTime + 100);
        } catch (Throwable ignored) {
        }
    }

    public static void spawnSandstorm(LivingEntity attacker) {
        if (!ModList.get().isLoaded("cataclysm") || attacker == null || attacker.level().isClientSide) return;

        try {
            CataInternal.doSpawnSandstorm(attacker);
        } catch (Throwable ignored) {
        }
    }

    private static class CataInternal {
        private static void doSpawn(LivingEntity attacker, LivingEntity target) {
            ScreenShake_Entity.ScreenShake(target.level(), target.position(), 20, 0.15f, 0, 30);
            
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(), 
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.5f, 
                1F / (attacker.getRandom().nextFloat() * 0.4F + 0.8F));

            com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity flameStrike = 
                new com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity(
                    target.level(), 
                    target.getX(), target.getY(), target.getZ(),
                    attacker.getYRot(), 
                    40,    // dur: 持续时间 (40 ticks = 2秒)
                    0,     // delay1
                    10,     // delay2
                    2.5F,  // radius: 半径
                    6.0F,  // dmg: 伤害
                    6.0F,  // 击退
                    true,  
                    attacker 
                );
            target.level().addFreshEntity(flameStrike);
        }

        private static void doSpawnSandstorm(LivingEntity attacker) {
            float yRot = attacker.yHeadRot; 
            float xRot = attacker.getXRot();

            float f = -Mth.sin(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
            float f1 = -Mth.sin(xRot * ((float)Math.PI / 180F));
            float f2 = Mth.cos(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
            
            double x = attacker.getX() - f * 0.2D; 
            double y = attacker.getEyeY() - 0.5D;  
            double z = attacker.getZ() - f2 * 0.2D;

            com.github.L_Ender.cataclysm.entity.projectile.Sandstorm_Projectile sandstorm = 
                new com.github.L_Ender.cataclysm.entity.projectile.Sandstorm_Projectile(
                    attacker, 
                    f, f1, f2, 
                    attacker.level(), 
                    6 
                );
            
            sandstorm.setState(1); 
            sandstorm.setPos(x, y, z);
            
            attacker.level().addFreshEntity(sandstorm);
        }
    }
}