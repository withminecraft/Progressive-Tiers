package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.capability.IMobTrait;
import net.Momo_EMT.enhanced_monster.capability.MobTraitProvider;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.Momo_EMT.enhanced_monster.item.TraitConfig;
import net.Momo_EMT.enhanced_monster.item.ModItems;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "enhanced_monster", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    private static final UUID REGEN_DEBUFF_UUID = UUID.fromString("7e7e1234-abcd-4f1a-8e9a-555555555555");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living) {
            String entityId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType()).toString();
            
            if (event.getObject() instanceof LivingEntity) {
                event.addCapability(ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "traits"), new MobTraitProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && !living.level().isClientSide) {
            living.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
                if (!cap.getTraits().isEmpty()) {
                    CompoundTag syncTag = cap.serializeNBT();
                    EnhancedMonster.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()), 
                        new PacketSyncMobTrait(living.getId(), syncTag));
                }
            });
        }
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

        if (entity.getPersistentData().getBoolean("em_loot_generated")) return;
        entity.getPersistentData().putBoolean("em_loot_generated", true);

        entity.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
            int quality = cap.getQuality();
            boolean isBoss = cap.isBoss();
            var traitsMap = cap.getTraits(); 

            if (traitsMap.isEmpty()) return;

            if (isBoss) {
                handleCustomDrops(entity, ModConfig.BOSS_EXTRA_DROPS.get());
            } else if (quality == 3) {
                handleCustomDrops(entity, ModConfig.QUALITY_3_EXTRA_DROPS.get());
            } else if (quality == 2) {
                handleCustomDrops(entity, ModConfig.QUALITY_2_EXTRA_DROPS.get());
            }

            RandomSource random = entity.getRandom();
            float dropChance = 0f;
            int bookLevel = 0;

            if (isBoss) {
                dropChance = 0.80f;
                bookLevel = 2; // 3级书
            } else if (quality == 3) {
                dropChance = 0.40f;
                bookLevel = 1; // 2级书
            } else if (quality == 2) {
                dropChance = 0.10f;
                bookLevel = 0; // 1级书
            }

            if (dropChance <= 0f) return;

            for (Map.Entry<String, Integer> entry : traitsMap.entrySet()) {
                String traitId = entry.getKey();

                if (!TraitConfig.hasItem(traitId)) continue;

                if (!isBoss && (traitId.equals(EffectAllocator.BERSERK) || traitId.equals(EffectAllocator.LIFESTEAL))) {
                    continue;
                }

                if (random.nextFloat() < dropChance) {
                    int finalLevel = Math.min(bookLevel, TraitConfig.getMaxLevel(traitId));
                    
                    ItemStack traitBook = ModItems.createTraitStack(traitId, finalLevel);
                    if (!traitBook.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(entity.level(), 
                            entity.getX(), entity.getY() + 0.5, entity.getZ(), traitBook);
                        itemEntity.setPickUpDelay(10);
                        itemEntity.setInvulnerable(true);
                        entity.level().addFreshEntity(itemEntity);
                    }
                }
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
            } catch (Exception ignored) {} 
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        Entity attackerEntity = event.getSource().getEntity();
        if (attackerEntity instanceof LivingEntity attacker) {
            CompoundTag vNbt = victim.getPersistentData();
            CompoundTag aNbt = attacker.getPersistentData();
            UUID vOwner = vNbt.contains("EM_Summoner_Owner") ? vNbt.getUUID("EM_Summoner_Owner") : null;
            UUID aOwner = aNbt.contains("EM_Summoner_Owner") ? aNbt.getUUID("EM_Summoner_Owner") : null;

            boolean isFamily = false;
            if (victim.getUUID().equals(aOwner)) isFamily = true;
            else if (attacker.getUUID().equals(vOwner)) isFamily = true;
            else if (vOwner != null && vOwner.equals(aOwner)) isFamily = true;

            if (isFamily) {
                event.setCanceled(true);
                if (attacker instanceof Mob mob && mob.getTarget() == victim) {
                    mob.setTarget(null);
                    mob.setLastHurtByMob(null);
                }
                return; 
            }
        }

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
        if (event.getEntity().level().isClientSide) return;

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
        if (event.getEntity().level().isClientSide) return;

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
                    victim.setTicksFrozen(400);
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

        if (entity.isAlive() && entity.tickCount % 20 == 0) {
            entity.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
                if (cap.getTraits().containsKey(EffectAllocator.REGENERATING)) {
                    handleCustomRegen(entity);
                }

                if (cap.getTraits().containsKey(EffectAllocator.SUMMONER)) {
                    if (!entity.getPersistentData().getBoolean("em_summoned") && 
                        entity.getHealth() <= entity.getMaxHealth() * 0.5f) {
                        
                        spawnSummons(entity);
                        entity.getPersistentData().putBoolean("em_summoned", true);
                    }
                }
            });
        }
    }

    private static void handleCustomRegen(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        long currentTime = entity.level().getGameTime();
        
        long cooldownEnd = data.getLong("EM_Regen_CD");
        long effectEnd = data.getLong("EM_Regen_Active_End");

        var maxHealthAttr = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;
        double originalMax = maxHealthAttr.getBaseValue();

        if (currentTime < effectEnd) {
            float healAmount = (float) (originalMax * 0.01f) + 1.0f;
            entity.heal(healAmount);
            // 粒子反馈
            if (entity.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getEyeY(), entity.getZ(), 4, 0.2, 0.2, 0.2, 0.0);
            }
        } 
        else if (currentTime >= cooldownEnd && entity.getHealth() <= (originalMax * 0.3f)) {
            
            data.putLong("EM_Regen_Active_End", currentTime + 600);
            data.putLong("EM_Regen_CD", currentTime + 1200);

            if (entity.getMaxHealth() > originalMax * 0.41) {
                double currentModifier = 0;
                var oldMod = maxHealthAttr.getModifier(REGEN_DEBUFF_UUID);
                if (oldMod != null) {
                    currentModifier = oldMod.getAmount();
                    maxHealthAttr.removeModifier(REGEN_DEBUFF_UUID);
                }
                
                maxHealthAttr.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        REGEN_DEBUFF_UUID, "Regen Penalty", currentModifier - (originalMax * 0.2), 
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
                
                if (entity.getHealth() > entity.getMaxHealth()) {
                    entity.setHealth(entity.getMaxHealth());
                }
            }
        }
    }

    private static void spawnSummons(LivingEntity parent) {
        if (!(parent.level() instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < 2; i++) {
            Entity summon = parent.getType().create(serverLevel);
            if (summon instanceof LivingEntity livingSummon) {
                livingSummon.getPersistentData().putBoolean("EM_SkipAllocation", true);
                livingSummon.getPersistentData().putUUID("EM_Summoner_Owner", parent.getUUID());
                
                if (parent instanceof Mob parentMob && livingSummon instanceof Mob summonMob) {
                    summonMob.setTarget(parentMob.getTarget());
                    summonMob.setLastHurtByMob(parentMob.getLastHurtByMob());

                    summonMob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(summonMob.blockPosition()), 
                                          net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED, null, null);
                }

                double angle = (parent.getYRot() + (i == 0 ? 135.0F : -135.0F)) * (Math.PI / 180.0D);
                double distance = 1.5D; 

                double offsetX = -Math.sin(angle) * distance;
                double offsetZ = Math.cos(angle) * distance;

                livingSummon.moveTo(parent.getX() + offsetX, 
                                    parent.getY(), 
                                    parent.getZ() + offsetZ, 
                                    parent.getYRot(), parent.getXRot());
                
                serverLevel.sendParticles(ParticleTypes.PORTAL, 
                    livingSummon.getX(), livingSummon.getY(0.5), livingSummon.getZ(), 
                    40, 0.5, 0.5, 0.5, 0.0);
                
                serverLevel.playSound(null, livingSummon.getX(), livingSummon.getY(), livingSummon.getZ(), 
                    net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
                    
                serverLevel.addFreshEntity(livingSummon);
            }
        }
    }
}