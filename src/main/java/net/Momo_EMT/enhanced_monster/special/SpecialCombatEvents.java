package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "enhanced_monster", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpecialCombatEvents {

    @SubscribeEvent
    public static void onWitherSkeletonAttack(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntity();

        if (attacker instanceof LivingEntity livingAttacker && 
            livingAttacker.getPersistentData().getBoolean(WitherSkeletonSpecial.TAG_DROP_IGNITIUM)) {
            
            if (target != null && target.level().random.nextFloat() < 0.30F) {
                CataclysmCompat.spawnFlameStrike(livingAttacker, target, target.level().getGameTime()); 
            }
        }
    }

@SubscribeEvent
public static void onHuskAttack(LivingHurtEvent event) {
    if (event.getEntity().level().isClientSide) return;

    Entity attacker = event.getSource().getEntity();
    
    if (attacker instanceof LivingEntity livingAttacker) {
        if (livingAttacker.getPersistentData().getBoolean(HuskSpecial.TAG_DROP_ANCIENT_LOOT)) {
            
            long currentTime = livingAttacker.level().getGameTime();
            String nbtKey = "last_sandstorm_tick";
            
            long lastTriggerTime = livingAttacker.getPersistentData().getLong(nbtKey);

            if (currentTime - lastTriggerTime >= 20) {
                CataclysmCompat.spawnSandstorm(livingAttacker);
                
                livingAttacker.getPersistentData().putLong(nbtKey, currentTime);
            }
        }
    }
}

    @SubscribeEvent
    public static void onSpiderAttack(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntity();

        if (attacker instanceof LivingEntity livingAttacker && 
            livingAttacker.getPersistentData().getBoolean(SpiderSpecial.TAG_WEB_ATTACK)) {
            
            if (target != null && !target.level().isClientSide) {
                // 在目标当前位置生成蜘蛛网
                net.minecraft.core.BlockPos pos = target.blockPosition();
                if (target.level().isEmptyBlock(pos)) {
                    target.level().setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.COBWEB.defaultBlockState());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPiglinShoot(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof Piglin piglin && 
                piglin.getPersistentData().getBoolean(PiglinSpecial.TAG_DROP_GOLD)) {
                if (arrow instanceof Arrow tippedArrow) {
                    tippedArrow.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onTridentJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ThrownTrident trident) {
            if (trident.getOwner() instanceof LivingEntity owner && 
                owner.getPersistentData().getBoolean(DrownedSpecial.TAG_DROP_TRIDENT)) {
                
                trident.getPersistentData().putBoolean("em_super_trident", true);
            }
        }
    }

    @SubscribeEvent
    public static void onTridentHit(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof ThrownTrident trident) {
            if (trident.getPersistentData().getBoolean("em_super_trident")) {
                LivingEntity victim = event.getEntity();
                
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(victim.level());
                if (lightning != null) {
                    lightning.moveTo(victim.position());
                    victim.level().addFreshEntity(lightning);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMountHurt(LivingHurtEvent event) {
        LivingEntity mount = event.getEntity();
        if (mount == null || mount.level().isClientSide) return;

        if (mount.getPersistentData().getBoolean("em_redirect_damage")) {
            Entity passenger = mount.getFirstPassenger();
            
            if (passenger instanceof LivingEntity rider && rider.isAlive()) {
                rider.hurt(event.getSource(), event.getAmount());
                
                event.setCanceled(true);
                event.setAmount(0);
                mount.invulnerableTime = 10;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEvokerDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        if (entity.getPersistentData().contains(EvokerSpecial.TAG_TOTEM_COUNT)) {
            int count = entity.getPersistentData().getInt(EvokerSpecial.TAG_TOTEM_COUNT);

            if (count > 0) {
                entity.getPersistentData().putInt(EvokerSpecial.TAG_TOTEM_COUNT, count - 1);

                event.setCanceled(true);

                entity.setHealth(1.0F); 
                entity.removeAllEffects();
                
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

                entity.level().broadcastEntityEvent(entity, (byte)35);
            }
        }
    }

    @SubscribeEvent
    public static void onSpecialDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        if (entity.getPersistentData().getBoolean(CreeperSpecial.TAG_DROP_HEAD)) {
            ItemStack head = new ItemStack(Items.CREEPER_HEAD);
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), head));
        }

        if (entity.getPersistentData().getBoolean(EvokerSpecial.TAG_DROP_TOTEM)) {
            int amount = 2;
            if (entity.level().random.nextFloat() < 0.5F) {
                amount += 1;
            }
            ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING, amount);
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), totem));
        }

        if (entity.getPersistentData().getBoolean(VindicatorSpecial.TAG_DROP_EMERALD)) {
            int amount = 4;
            if (entity.level().random.nextFloat() < 0.5F) {
                amount += 1;
            }
            ItemStack emeraldBlock = new ItemStack(Items.EMERALD_BLOCK, amount);
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), emeraldBlock));
        }

        if (entity.getPersistentData().getBoolean(PiglinBruteSpecial.TAG_DROP_SCRAP)) {
            ItemStack scrap = new ItemStack(Items.ANCIENT_DEBRIS);
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), scrap));
        }

        if (entity.getPersistentData().getBoolean(DrownedSpecial.TAG_DROP_TRIDENT)) {
            ItemStack trident = new ItemStack(Items.TRIDENT);
            
            int maxDamage = trident.getMaxDamage();
            float randomPercent = 0.2F + entity.level().random.nextFloat() * 0.4F; 
            int damageToSet = maxDamage - (int)(maxDamage * randomPercent);
            
            trident.setDamageValue(damageToSet);

            event.getDrops().add(new ItemEntity(
                entity.level(), 
                entity.getX(), entity.getY(), entity.getZ(), 
                trident
            ));
        }

        if (entity.getPersistentData().getBoolean(WitherSkeletonSpecial.TAG_DROP_SKULL)) {
            int amount = 1 + (entity.level().random.nextFloat() < 0.5F ? 1 : 0);
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 
                new ItemStack(Items.WITHER_SKELETON_SKULL, amount)));
        }

        if (entity.getPersistentData().getBoolean(WitherSkeletonSpecial.TAG_DROP_IGNITIUM)) {
            if (entity.level().getRandom().nextFloat() < 0.5f) {
                Item ignitiumIngot = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("cataclysm:ignitium_ingot"));
                if (ignitiumIngot != null) {
                    event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 
                        new ItemStack(ignitiumIngot)));
                }
            }
        }

        if (entity.getPersistentData().getBoolean(PiglinSpecial.TAG_DROP_GOLD)) {
            int amount = 4;
            if (entity.level().random.nextFloat() < 0.5F) {
                amount += 1;
            }
            
            ItemStack goldBlocks = new ItemStack(Items.GOLD_BLOCK, amount);
            event.getDrops().add(new ItemEntity(
                entity.level(), 
                entity.getX(), entity.getY(), entity.getZ(), 
                goldBlocks
            ));
        }

        if (entity.getPersistentData().getBoolean(ZombifiedPiglinSpecial.TAG_DROP_GOLD)) {
            int amount = 3;
            if (entity.level().random.nextFloat() < 0.5F) {
                amount += 1;
            }
            
            ItemStack goldBlocks = new ItemStack(Items.GOLD_BLOCK, amount);
            event.getDrops().add(new ItemEntity(
                entity.level(), 
                entity.getX(), entity.getY(), entity.getZ(), 
                goldBlocks
            ));
        }

        if (entity.getPersistentData().getBoolean(PillagerSpecial.TAG_DROP_EMERALD)) {
            ItemStack emeraldBlock = new ItemStack(Items.EMERALD_BLOCK, 4);
            event.getDrops().add(new ItemEntity(
                entity.level(), 
                entity.getX(), entity.getY(), entity.getZ(), 
                emeraldBlock
            ));
        }

        if (entity.getPersistentData().getBoolean(HuskSpecial.TAG_DROP_ANCIENT_LOOT)) {
            Item ancientMetal = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("cataclysm:ancient_metal_ingot"));
            if (ancientMetal != null) {
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 
                    new ItemStack(ancientMetal, 3)));
            }

            Item kobolediatorSkull = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("cataclysm:kobolediator_skull"));
            if (kobolediatorSkull != null) {
                event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 
                    new ItemStack(kobolediatorSkull, 1)));
            }
        }
    }
}