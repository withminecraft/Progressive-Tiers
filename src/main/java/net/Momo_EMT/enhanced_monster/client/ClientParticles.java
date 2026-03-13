package net.Momo_EMT.enhanced_monster.client;

import net.Momo_EMT.enhanced_monster.ModConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

public class ClientParticles {
    
    public static void spawnParticles(LivingEntity entity, int quality, boolean isBoss) {
        if (!ModConfig.ENABLE_PARTICLES.get()) return;

        var level = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();

        // --- Boss 专属效果：自适应双螺旋 ---
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
        
        // --- Tier 3 ：虚空灵魂与能量崩塌 ---
        else if (quality == 3) {
            if (level.random.nextFloat() < 0.6f) {
                double rx = x + (level.random.nextDouble() - 0.5) * (width * 1.1);
                double rz = z + (level.random.nextDouble() - 0.5) * (width * 1.1);
                
                double soulSpeed = height * 0.02; 
                level.addParticle(ParticleTypes.SOUL, rx, y + 0.2, rz, 0, soulSpeed, 0);
            }

            if (level.random.nextFloat() < 0.85f) { 
                for (int i = 0; i < 3; i++) {
                    double angle = level.random.nextDouble() * Math.PI * 2;
                    double dist = width + 0.7; 
                    double startX = x + Math.cos(angle) * dist;
                    double startZ = z + Math.sin(angle) * dist;
                    double startY = y + level.random.nextDouble() * height ;

                    level.addParticle(ParticleTypes.REVERSE_PORTAL, startX, startY, startZ, 0, 0, 0);
                }
            }
        }
        
        // --- Tier 2 ：蓝火环绕 ---
        else if (quality == 2) {
            if (level.random.nextFloat() < 0.6f) {
                double rx = x + (level.random.nextDouble() - 0.5) * (width * 1.1);
                double rz = z + (level.random.nextDouble() - 0.5) * (width * 1.1);
                
                double startY = y + 0.1;
                
                double fireSpeed = height * 0.03;
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, rx, startY, rz, 0, fireSpeed, 0);
            }
    }
    }
}
