package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber
public class SpecialHorseHandler {
    public static final String TAG_SPAWN_TICK = "em_special_spawn_tick";
    private static final int DESPAWN_DELAY = 3600;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        var entity = event.getEntity();

        if (entity.level().isClientSide) return;
        if (!(entity instanceof LivingEntity living)) return;
        
        if (living.tickCount % 20 != 0) return;
        
        if (living instanceof ZombieHorse || living instanceof SkeletonHorse) {
            AbstractHorse horse = (AbstractHorse) living;
            CompoundTag nbt = horse.getPersistentData();

            if (!nbt.contains(TAG_SPAWN_TICK)) return;

            if (horse.isSaddled()) {
                nbt.remove(TAG_SPAWN_TICK);
                horse.setPersistenceRequired(); 
                return;
            }

            if (horse.isVehicle()) {
                nbt.putLong(TAG_SPAWN_TICK, horse.level().getGameTime());
            } else {
                long spawnTime = nbt.getLong(TAG_SPAWN_TICK);
                long currentTime = horse.level().getGameTime();

                if (currentTime - spawnTime > DESPAWN_DELAY) {
                    horse.discard(); 
                }
            }
        }
    }
}