package net.Momo_EMT.enhanced_monster.client;

import net.Momo_EMT.enhanced_monster.ModConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ClientParticles {
    public static void spawnParticles(LivingEntity entity, int quality, boolean isBoss) {
        if (!ModConfig.ENABLE_PARTICLES.get()) return;

        Level level = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();

        if (isBoss) {
            for (int i = 0; i < 2; i++) {
                double angle = (entity.tickCount * 0.12) + (i * Math.PI);
                double radius = width * 0.85; 
                double px = x + Math.cos(angle) * radius;
                double pz = z + Math.sin(angle) * radius;
                double py = y + (height * 0.2) + Math.sin(entity.tickCount * 0.05) * 0.2;
                level.addParticle(ParticleTypes.DRAGON_BREATH, px, py, pz, 0, 0.02, 0);
            }
            if (entity.tickCount % 8 == 0) {
                level.addParticle(ParticleTypes.WITCH, x, y + height * 0.9, z, 0, 0, 0);
            }
        } 
        else if (quality == 3) {
            if (level.random.nextFloat() < 0.6f) {
                double rx = x + (level.random.nextDouble() - 0.5) * (width * 1.1);
                double rz = z + (level.random.nextDouble() - 0.5) * (width * 1.1);
                level.addParticle(ParticleTypes.SOUL, rx, y + 0.2, rz, 0, height * 0.02, 0);
            }
            if (level.random.nextFloat() < 0.8f) { 
                for (int i = 0; i < 3; i++) {
                    double angle = level.random.nextDouble() * Math.PI * 2;
                    double startX = x + Math.cos(angle) * (width + 0.7);
                    double startZ = z + Math.sin(angle) * (width + 0.7);
                    level.addParticle(ParticleTypes.REVERSE_PORTAL, startX, y + level.random.nextDouble() * height, startZ, 0, 0, 0);
                }
            }
        }
        else if (quality == 2) {
            if (level.random.nextFloat() < 0.6f) {
                double rx = x + (level.random.nextDouble() - 0.5) * (width * 1.1);
                double rz = z + (level.random.nextDouble() - 0.5) * (width * 1.1);
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, rx, y + 0.1, rz, 0, height * 0.03, 0);
            }
        }
    }
}