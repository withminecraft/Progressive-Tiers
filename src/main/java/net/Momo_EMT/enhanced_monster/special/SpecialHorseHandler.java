package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SpecialHorseHandler {
    public static final String TAG_SPAWN_TICK = "em_special_spawn_tick";
    private static final int DESPAWN_DELAY = 3600;

    @SubscribeEvent
    public static void onHorseTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getEntity().tickCount % 20 != 0) return;
        
        if (event.getEntity() instanceof ZombieHorse || event.getEntity() instanceof SkeletonHorse) {
            AbstractHorse horse = (AbstractHorse) event.getEntity();
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