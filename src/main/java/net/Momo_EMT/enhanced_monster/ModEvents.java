package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.capability.IMobTrait;
import net.Momo_EMT.enhanced_monster.capability.MobTraitProvider;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.monster.Enemy;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "enhanced_monster", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living) {
            String entityId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType()).toString();
            
            boolean isListedBoss = ModConfig.isBoss(entityId); 
            
            if (isListedBoss || (living instanceof Enemy)) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "traits"), new MobTraitProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && !living.level().isClientSide) {
            living.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
                if (cap.isProcessed() && cap.getQuality() > 0) {
                    CompoundTag syncTag = cap.serializeNBT();
                    EnhancedMonster.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), 
                        new PacketSyncMobTrait(living.getId(), syncTag));
                }
            });
        }
    }

    private static List<Enchantment> CACHED_ENCHANTMENTS;

    public static void clearEnchantmentCache() {
        CACHED_ENCHANTMENTS = null;
    }

    private static List<Enchantment> getAvailableEnchantments() {
        if (CACHED_ENCHANTMENTS == null) {
            CACHED_ENCHANTMENTS = ForgeRegistries.ENCHANTMENTS.getValues().stream()
                .filter(ench -> ench.isAllowedOnBooks() && !ench.isCurse() && !ench.isTreasureOnly())
                .toList();
        }
        return CACHED_ENCHANTMENTS;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living && !event.getLevel().isClientSide && living.isAlive()) {
            EffectAllocator.apply(living);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMobDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (entity.getPersistentData().getBoolean("em_loot_generated")) {
            return;
        }
        entity.getPersistentData().putBoolean("em_loot_generated", true);

        entity.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
            int quality = cap.getQuality();
            boolean isBoss = cap.isBoss();
            if (quality < 2 && !isBoss) return;

            if (isBoss) {
                handleCustomDrops(entity, ModConfig.BOSS_EXTRA_DROPS.get());
            } else if (quality == 3) {
                handleCustomDrops(entity, ModConfig.QUALITY_3_EXTRA_DROPS.get());
            } else if (quality == 2) {
                handleCustomDrops(entity, ModConfig.QUALITY_2_EXTRA_DROPS.get());
            }

            if (!ModConfig.ENABLE_DROPS.get()) return;
            
            RandomSource random = entity.getRandom();
            int dropCount = 0;
            if (isBoss) {
                dropCount = 3;
            } else if (quality == 3) {
                dropCount = 1 + random.nextInt(2);
            } else if (quality == 2) {
                dropCount = random.nextFloat() < 0.5f ? 1 : 0;
            }

            if (dropCount <= 0) return;

            List<Enchantment> available = getAvailableEnchantments();
            if (available.isEmpty()) return;

            List<Enchantment> filteredForQuality2 = null;
            if (quality == 2 && !isBoss) {
                filteredForQuality2 = available.stream().filter(ench -> ench.getMaxLevel() > 1).toList();
            }

            for (int i = 0; i < dropCount; i++) {
                List<Enchantment> currentPool = (quality == 2 && !isBoss) ? filteredForQuality2 : available;
                if (currentPool == null || currentPool.isEmpty()) break;

                Enchantment randomEnch = currentPool.get(random.nextInt(currentPool.size()));
                int maxLvl = randomEnch.getMaxLevel();
                int level;

                if (isBoss) {
                    level = Math.max(1, maxLvl - 1);
                } else if (quality == 3) {
                    int min = Math.max(1, maxLvl / 2);
                    int range = Math.max(1, maxLvl - min); 
                    level = min + (range > 0 ? random.nextInt(range) : 0);
                } else {
                    level = 1 + random.nextInt(Math.max(1, maxLvl / 2));
                }

                ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.setEnchantments(Map.of(randomEnch, level), enchantedBook);
                ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), enchantedBook);
                itemEntity.setPickUpDelay(10);
                itemEntity.setInvulnerable(true);
                entity.level().addFreshEntity(itemEntity);
            }
        });
    }

    private static void handleCustomDrops(LivingEntity entity, List<? extends String> dropList) {
        if (dropList == null || dropList.isEmpty()) return;
        RandomSource random = entity.getRandom();

        for (String entry : dropList) {
            try {
                String[] parts = entry.split(",");
                if (parts.length < 4) continue;

                String itemId = parts[0].trim();
                int min = Integer.parseInt(parts[1].trim());
                int max = Integer.parseInt(parts[2].trim());
                float chance = Float.parseFloat(parts[3].trim());

                if (random.nextFloat() <= chance) {
                    var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemId));
                    if (item != null && item != Items.AIR) {
                        int count = min >= max ? min : min + random.nextInt(max - min + 1);
                        if (count > 0) {
                            ItemStack stack = new ItemStack(item, count);
                            ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), stack);
                            itemEntity.setPickUpDelay(10);
                            entity.level().addFreshEntity(itemEntity);
                        }
                    }
                }
            } catch (Exception ignored) {} // 忽略配置格式错误的行
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        victim.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
            if (cap.getTraits().containsKey(EffectAllocator.FIRE_PROT) && 
                event.getSource().is(DamageTypeTags.IS_FIRE)) {
                
                event.setCanceled(true);
                victim.setRemainingFireTicks(0);
                victim.clearFire();
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        LivingEntity attacker = getAttacker(event.getSource().getEntity(), event.getSource().getDirectEntity());
        final float[] damage = {event.getAmount()};

        if (attacker != null && attacker.isAlive()) {
            attacker.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(atkCap -> {
                if (atkCap.getTraits().containsKey(EffectAllocator.BERSERK)) {
                    damage[0] = damage[0] * 1.5f; 
                    
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.5), victim.getZ(), 10, 0.1, 0.1, 0.1, 0.5);
                    }
                }
            });
        }

        if (damage[0] != event.getAmount()) {
            event.setAmount(damage[0]);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getAmount() <= 0 || event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        LivingEntity attacker = getAttacker(event.getSource().getEntity(), event.getSource().getDirectEntity());

        victim.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
            Map<String, Integer> traits = cap.getTraits();
            if (traits.containsKey(EffectAllocator.PROTECTED)) {
                int level = traits.get(EffectAllocator.PROTECTED) + 1;
                float reduction = Math.max(0.1f, 1.0f - (level * 0.1f));
                event.setAmount(event.getAmount() * reduction);
            }
        });

        if (attacker != null && attacker.isAlive()) {
            attacker.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
                Map<String, Integer> traits = cap.getTraits();
                
                if (traits.containsKey(EffectAllocator.LIFESTEAL) && attacker.getHealth() < attacker.getMaxHealth()) {
                    float healAmount = Math.min(event.getAmount() * 0.5f, 12.0f);
                    attacker.heal(healAmount);
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HEART, attacker.getX(), attacker.getY(0.5), attacker.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
                    }
                }

                // 新增：VOID (虚无) 效果
                if (traits.containsKey(EffectAllocator.VOID)) {
                    int level = traits.get(EffectAllocator.VOID) + 1;
                    float voidExtraDamage = victim.getMaxHealth() * (level * 0.04f);
                    event.setAmount(event.getAmount() + voidExtraDamage);
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, victim.getX(), victim.getY(0.5), victim.getZ(), 10, 0.5, 0.5, 0.5, 0);
                    }
                }

                if (traits.containsKey(EffectAllocator.POISONOUS)) {
                    int amp = traits.get(EffectAllocator.POISONOUS);
                    victim.addEffect(new MobEffectInstance(MobEffects.POISON, 200, amp));
                    if (amp >= 2) { 
                        victim.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, amp - 2));
                    }
                }

                if (traits.containsKey(EffectAllocator.STRAY)) {
                    int amp = traits.get(EffectAllocator.STRAY);
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, amp));
                }

                if (traits.containsKey(EffectAllocator.WEAKENER)) {
                    int amp = traits.get(EffectAllocator.WEAKENER);
                    victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, amp));
                }
            });
        }
    }
    
    private static LivingEntity getAttacker(Entity sourceEntity, Entity directEntity) {
        if (sourceEntity instanceof LivingEntity e) return e;
        if (directEntity instanceof Projectile p && p.getOwner() instanceof LivingEntity e) return e;
        return null;
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (entity.level().isClientSide) {
            CompoundTag nbt = entity.getPersistentData();
            if (nbt.contains(EffectAllocator.TAG_QUALITY)) {
                int quality = nbt.getInt(EffectAllocator.TAG_QUALITY);
                boolean isBoss = nbt.getBoolean("IsBoss");
                if (quality >= 2 || isBoss) {
                    net.Momo_EMT.enhanced_monster.client.ClientParticles.spawnParticles(entity, quality, isBoss);
                }
            }
            return; 
        }

        if (entity.isAlive() && entity.tickCount % 20 == 0 && entity.getHealth() < entity.getMaxHealth()) {
            entity.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
                Integer regenLevel = cap.getTraits().get(EffectAllocator.REGENERATING);
                if (regenLevel != null) {
                    entity.heal((regenLevel + 1) * 0.5f);
                }
            });
        }
    }
}