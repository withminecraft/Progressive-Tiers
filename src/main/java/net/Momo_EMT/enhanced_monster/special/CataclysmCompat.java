package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.fml.ModList;

public class CataclysmCompat {

    public static void spawnFlameStrike(LivingEntity attacker, LivingEntity target, long gameTime) {
        if (!ModList.get().isLoaded("cataclysm")) return;

        long nextReadyTime = attacker.getPersistentData().getLong("em_flame_strike_cooldown");
        if (gameTime < nextReadyTime) return;

        try {
            CataInternal.doSpawn(attacker, target);
            attacker.getPersistentData().putLong("em_flame_strike_cooldown", gameTime + 100);
        } catch (Throwable ignored) {}
    }

    public static void spawnSandstorm(LivingEntity attacker) {
        if (!ModList.get().isLoaded("cataclysm") || attacker == null || attacker.level().isClientSide) return;

        try {
            CataInternal.doSpawnSandstorm(attacker);
        } catch (Throwable ignored) {}
    }

    private static class CataInternal {
        private static void doSpawn(LivingEntity attacker, LivingEntity target) {
            com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity.ScreenShake(target.level(), target.position(), 20, 0.15f, 0, 30);
            
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(), 
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.5f, 
                1F / (attacker.getRandom().nextFloat() * 0.4F + 0.8F));

            var flameStrike = new com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity(
                target.level(), 
                target.getX(), target.getY(), target.getZ(),
                attacker.getYRot(), 
                40, 0, 10, 2.5F, 6.0F, 6.0F, true, attacker 
            );
            target.level().addFreshEntity(flameStrike);
        }

        private static void doSpawnSandstorm(LivingEntity attacker) {
            float yRot = attacker.yHeadRot; 
            float xRot = attacker.getXRot();

            float f = -Mth.sin(yRot * (float)(Math.PI / 180.0)) * Mth.cos(xRot * (float)(Math.PI / 180.0));
            float f1 = -Mth.sin(xRot * (float)(Math.PI / 180.0));
            float f2 = Mth.cos(yRot * (float)(Math.PI / 180.0)) * Mth.cos(xRot * (float)(Math.PI / 180.0));
            
            var sandstorm = new com.github.L_Ender.cataclysm.entity.projectile.Sandstorm_Projectile(
                attacker, f, f1, f2, attacker.level(), 6 
            );
            
            sandstorm.setState(1); 
            sandstorm.setPos(attacker.getX() - f * 0.2, attacker.getEyeY() - 0.5, attacker.getZ() - f2 * 0.2);
            attacker.level().addFreshEntity(sandstorm);
        }
    }
}