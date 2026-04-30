package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.capability.MobTraitAttachment;
import net.Momo_EMT.enhanced_monster.capability.MobTraitData;
import net.Momo_EMT.enhanced_monster.item.ModItems;
import net.Momo_EMT.enhanced_monster.item.TraitConfig;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = "enhanced_monster")
public class ModEvents {
    private static final ResourceLocation REGEN_PENALTY_ID = ResourceLocation.fromNamespaceAndPath("enhanced_monster", "regen_max_health_penalty");

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && !living.level().isClientSide) {
            MobTraitData data = living.getData(MobTraitAttachment.MOB_TRAIT);
            if (!data.getTraits().isEmpty()) {
                CompoundTag syncTag = data.serializeNBT(); 
                PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), 
                    new PacketSyncMobTrait(living.getId(), syncTag));
            }
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

        MobTraitData data = entity.getData(MobTraitAttachment.MOB_TRAIT);
        int quality = data.getQuality();
        boolean isBoss = data.isBoss();
        Map<String, Integer> traitsMap = data.getTraits();

        if (traitsMap.isEmpty()) return;

        if (isBoss) handleCustomDrops(entity, ModConfig.BOSS_EXTRA_DROPS.get());
        else if (quality == 3) handleCustomDrops(entity, ModConfig.QUALITY_3_EXTRA_DROPS.get());
        else if (quality == 2) handleCustomDrops(entity, ModConfig.QUALITY_2_EXTRA_DROPS.get());

        RandomSource random = entity.getRandom();
        float dropChance = 0f;
        int bookLevel = 0;

        if (isBoss) {
            dropChance = 0.80f;
            bookLevel = 2; // 3级书 (索引2)
        } else if (quality == 3) {
            dropChance = 0.40f;
            bookLevel = 1; // 2级书 (索引1)
        } else if (quality == 2) {
            dropChance = 0.10f;
            bookLevel = 0; // 1级书 (索引0)
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
    }

    private static void handleCustomDrops(LivingEntity entity, List<? extends String> dropList) {
        if (dropList == null || dropList.isEmpty()) return;
        RandomSource random = entity.getRandom();
        for (String entry : dropList) {
            try {
                String[] parts = entry.split(",");
                if (parts.length < 4) continue;
                ResourceLocation itemId = ResourceLocation.parse(parts[0].trim());
                int min = Integer.parseInt(parts[1].trim());
                int max = Integer.parseInt(parts[2].trim());
                float chance = Float.parseFloat(parts[3].trim());
                if (random.nextFloat() <= chance) {
                    var item = BuiltInRegistries.ITEM.get(itemId);
                    if (item != Items.AIR) {
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
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        MobTraitData data = victim.getData(MobTraitAttachment.MOB_TRAIT);
        Map<String, Integer> traits = data.getTraits();

        if (data.getTraits().containsKey(EffectAllocator.FIRE_PROT) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setCanceled(true);
            victim.setRemainingFireTicks(0);
            victim.clearFire();
            return;
        }

        Entity attackerEntity = event.getSource().getEntity();
        if (attackerEntity instanceof LivingEntity attacker) {
            CompoundTag vNbt = victim.getPersistentData();
            CompoundTag aNbt = attacker.getPersistentData();
            UUID vOwner = vNbt.hasUUID("EM_Summoner_Owner") ? vNbt.getUUID("EM_Summoner_Owner") : null;
            UUID aOwner = aNbt.hasUUID("EM_Summoner_Owner") ? aNbt.getUUID("EM_Summoner_Owner") : null;
            boolean isFamily = victim.getUUID().equals(aOwner) || attacker.getUUID().equals(vOwner) || (vOwner != null && vOwner.equals(aOwner));

            if (isFamily) {
                event.setCanceled(true);
                if (attacker instanceof Mob mob && mob.getTarget() == victim) {
                    mob.setTarget(null);
                    mob.setLastHurtByMob(null);
                }
                return;
            }

            if (traits.containsKey(EffectAllocator.ELUSIVE)) {
                int level = traits.get(EffectAllocator.ELUSIVE) + 1;
                float dodgeChance = level * 0.10f; 

                if (victim.getRandom().nextFloat() < dodgeChance) {
                    event.setCanceled(true);

                    if (victim.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.POOF, victim.getX(), victim.getY(0.5), victim.getZ(), 20, 1, 1, 1, 0);
                    }
                }
            }

            MobTraitData atkData = attacker.getData(MobTraitAttachment.MOB_TRAIT);
            Map<String, Integer> atkTraits = atkData.getTraits();
            float currentAmount = event.getAmount();

            if (atkTraits.containsKey(EffectAllocator.POWERFUL)) {
                boolean isEnhanced = event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)
                                  || event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                                  || event.getSource().is(DamageTypeTags.IS_EXPLOSION)
                                  || event.getSource().is(DamageTypeTags.IS_FIRE)
                                  || event.getSource().is(DamageTypeTags.IS_FREEZING)
                                  || event.getSource().is(DamageTypeTags.IS_LIGHTNING);
                if (isEnhanced) {
                    int level = atkTraits.get(EffectAllocator.POWERFUL) + 1;
                    currentAmount = currentAmount * (1.0f + (level * 0.05f));
                }
            }

            if (atkData.getTraits().containsKey(EffectAllocator.BERSERK)) {
                currentAmount = currentAmount * 1.5f;
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.5), victim.getZ(), 10, 0.2, 0.2, 0.2, 0);
                }
            }

            if (atkTraits.containsKey(EffectAllocator.EROSIVE)) {
                DamageSource source = event.getSource();
                int level = atkTraits.get(EffectAllocator.EROSIVE) + 1;
                int durabilityDamage = level * 5;

                if (victim.isDamageSourceBlocked(source)) {
                    ItemStack mainHand = victim.getMainHandItem();
                    ItemStack offHand = victim.getOffhandItem();
                    ItemStack shield = ItemStack.EMPTY;
                    EquipmentSlot slot = null;

                    if (mainHand.getItem() instanceof net.minecraft.world.item.ShieldItem) {
                        shield = mainHand;
                        slot = EquipmentSlot.MAINHAND;
                    } else if (offHand.getItem() instanceof net.minecraft.world.item.ShieldItem) {
                        shield = offHand;
                        slot = EquipmentSlot.OFFHAND;
                    } 

                    if (!shield.isEmpty()) {
                        shield.hurtAndBreak(durabilityDamage * 4, victim, slot);
                    }
                }
            }

            if (currentAmount != event.getAmount()) {
                event.setAmount(currentAmount);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        if (victim.level().isClientSide) return;

        MobTraitData vicData = victim.getData(MobTraitAttachment.MOB_TRAIT);
        Map<String, Integer> traits = vicData.getTraits();
        
        if (traits.containsKey(EffectAllocator.PROTECTED)) {
            int level = traits.get(EffectAllocator.PROTECTED) + 1;
            float reduction = Math.max(0.0f, 1.0f - (level * 0.1f));
            event.setNewDamage(event.getNewDamage() * reduction);
        }

        if (victim.isDamageSourceBlocked(source)) return;

        LivingEntity attacker = getAttacker(event.getSource().getEntity(), event.getSource().getDirectEntity());
        if (attacker != null && attacker.isAlive()) {
            MobTraitData atkData = attacker.getData(MobTraitAttachment.MOB_TRAIT);
            Map<String, Integer> atkTraits = atkData.getTraits();

            if (atkTraits.containsKey(EffectAllocator.LIFESTEAL) && attacker.getHealth() < attacker.getMaxHealth()) {
                float healAmount = Math.min(event.getNewDamage() * 0.5f, 20.0f);
                attacker.heal(healAmount);
                if (attacker.level() instanceof ServerLevel serverLevel && healAmount > 0) {
                    serverLevel.sendParticles(ParticleTypes.HEART, attacker.getX(), attacker.getY(0.5), attacker.getZ(), 5, 0.3, 0.3, 0.3, 0);
                }
            }

            if (atkTraits.containsKey(EffectAllocator.VOID)) {
                long currentTime = attacker.level().getGameTime();
                long lastTriggered = atkData.getVoidCooldown(); 

                if (currentTime - lastTriggered >= 10) {
                    int level = atkTraits.get(EffectAllocator.VOID) + 1;
                    event.setNewDamage(event.getNewDamage() + (victim.getMaxHealth() * (level * 0.04f)));
                    
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, victim.getX(), victim.getY(0.5), victim.getZ(), 10, 0.5, 0.5, 0.5, 0);
                    }

                    atkData.setVoidCooldown(currentTime);

                    syncAndSave(attacker, atkData); 
                }
            }

            if (atkTraits.containsKey(EffectAllocator.EROSIVE)) {
                int level = atkTraits.get(EffectAllocator.EROSIVE) + 1;
                int durabilityDamage = level * 5;

                List<EquipmentSlot> activeSlots = new java.util.ArrayList<>();
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.isArmor() && !victim.getItemBySlot(slot).isEmpty()) {
                        activeSlots.add(slot);
                    }
                }

                if (!activeSlots.isEmpty()) {
                    EquipmentSlot targetSlot = activeSlots.get(attacker.getRandom().nextInt(activeSlots.size()));
                    ItemStack armorStack = victim.getItemBySlot(targetSlot);
                    armorStack.hurtAndBreak(durabilityDamage, victim, targetSlot);
                }

                if (victim instanceof net.minecraft.world.entity.player.Player player) {
                    if (attacker.getRandom().nextFloat() < 0.5f) {
                        int reduction = level * 2;
                        var foodData = player.getFoodData();
                            
                        foodData.setFoodLevel(Math.max(0, foodData.getFoodLevel() - reduction));
                            
                        foodData.setSaturation(Math.max(0.0f, foodData.getSaturationLevel() - (float)reduction));

                        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, level - 1));
                    }
                } else {
                        if (attacker.getRandom().nextFloat() < 0.5f) {
                            float extraDamage = (float) level;
                            event.setNewDamage(event.getNewDamage() + extraDamage);
                        }
                    }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        LivingEntity attacker = getAttacker(event.getSource().getEntity(), event.getSource().getDirectEntity());

        if (victim.isDamageSourceBlocked(source)) return;

        if (attacker != null && attacker.isAlive()) {
            MobTraitData atkData = attacker.getData(MobTraitAttachment.MOB_TRAIT);
            Map<String, Integer> atkTraits = atkData.getTraits();

            if (atkTraits.containsKey(EffectAllocator.POISONOUS)) {
                int amp = atkTraits.get(EffectAllocator.POISONOUS);
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 200, amp));
                if (amp >= 1) victim.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 400, amp - 1));
            }

            if (atkTraits.containsKey(EffectAllocator.STRAY)) {
                int amp = atkTraits.get(EffectAllocator.STRAY);
                victim.setTicksFrozen(400);
                if (amp >= 1) victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, amp - 1));
            }

            if (atkTraits.containsKey(EffectAllocator.WEAKENER)) {
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, atkTraits.get(EffectAllocator.WEAKENER)));
            }

            if (atkTraits.containsKey(EffectAllocator.WITHERING)) {
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, atkTraits.get(EffectAllocator.WITHERING)));

                MobTraitData vicData = victim.getData(MobTraitAttachment.MOB_TRAIT);
                vicData.setHealInhibitTicks(100);
                syncAndSave(victim, vicData);
            }
        }
    }

    private static LivingEntity getAttacker(Entity sourceEntity, Entity directEntity) {
        if (sourceEntity instanceof LivingEntity e) return e;
        if (directEntity instanceof Projectile p && p.getOwner() instanceof LivingEntity e) return e;
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        MobTraitData data = entity.getData(MobTraitAttachment.MOB_TRAIT);
        if (data.getHealInhibitTicks() > 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide) {
            MobTraitData cap = entity.getData(MobTraitAttachment.MOB_TRAIT);
            
            if (cap != null && cap.isProcessed()) { 
                int quality = cap.getQuality(); 
                boolean isBoss = cap.isBoss(); 
                
                if (quality >= 2 || isBoss) {
                    net.Momo_EMT.enhanced_monster.client.ClientParticles.spawnParticles(entity, quality, isBoss);
                }
            }
            return;
        }

        if (entity.isAlive()) {
            MobTraitData data = entity.getData(MobTraitAttachment.MOB_TRAIT);

            int currentInhibit = data.getHealInhibitTicks();
            if (currentInhibit > 0) data.setHealInhibitTicks(currentInhibit - 1);
            
            if (data.getTraits().containsKey(EffectAllocator.REGENERATING)) {
                handleCustomRegen(entity);
            }
            
            if (data.getTraits().containsKey(EffectAllocator.SUMMONER)) {
                if (!entity.getPersistentData().getBoolean("em_summoned") && 
                    entity.getHealth() <= entity.getMaxHealth() * 0.5f) {
                    spawnSummons(entity);
                    entity.getPersistentData().putBoolean("em_summoned", true);
                }
            }
        }
    }

    private static void handleCustomRegen(LivingEntity entity) {
        MobTraitData data = entity.getData(MobTraitAttachment.MOB_TRAIT);

        if (data.getInitialMaxHealth() <= 0) {
            data.setInitialMaxHealth(entity.getMaxHealth());
        }
        double originalMax = data.getInitialMaxHealth();
        
        int activeTicks = data.getRegenActiveTicks(); 
        int cooldownTicks = data.getRegenCooldownTicks();

        var maxHealthInstance = entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthInstance == null) return;

        if (activeTicks > 0) {
            data.setRegenActiveTicks(activeTicks - 1);

            if (entity.tickCount % 20 == 0) {
                int traitLevel = data.getTraits().getOrDefault(EffectAllocator.REGENERATING, 0) + 1;
                float healAmount = (float) (entity.getMaxHealth() * (0.01f * traitLevel)) + 2.0f;
                entity.heal(healAmount);
                if (entity.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getEyeY(), entity.getZ(), 4, 0.2, 0.2, 0.2, 0);
                }
            }
        } 
        else if (cooldownTicks <= 0 && entity.getHealth() <= (float) (originalMax * 0.3f)) {
            data.setRegenActiveTicks(600); 
            data.setRegenCooldownTicks(1200);

            if (entity.getMaxHealth() > originalMax * 0.41) {
                double currentPenalty = 0;
                var existingMod = maxHealthInstance.getModifier(REGEN_PENALTY_ID);
                if (existingMod != null) {
                    currentPenalty = existingMod.amount();
                    maxHealthInstance.removeModifier(REGEN_PENALTY_ID);
                }

                maxHealthInstance.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        REGEN_PENALTY_ID, 
                        currentPenalty - (originalMax * 0.2), 
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
                
                if (entity.getHealth() > entity.getMaxHealth()) {
                    entity.setHealth(entity.getMaxHealth());
                }
            }
            
            syncAndSave(entity, data); 
        }
        
        if (cooldownTicks > 0) {
            data.setRegenCooldownTicks(cooldownTicks - 1);
        }
    }

    private static void syncAndSave(LivingEntity entity, MobTraitData data) {
        entity.setData(MobTraitAttachment.MOB_TRAIT, data);

        CompoundTag syncTag = data.serializeNBT();

        if (!entity.level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(entity, 
                new PacketSyncMobTrait(entity.getId(), syncTag));
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
                        net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED, null);
                }
                double angle = (parent.getYRot() + (i == 0 ? 135.0F : -135.0F)) * (Math.PI / 180.0D);
                livingSummon.moveTo(parent.getX() - Math.sin(angle) * 1.5, parent.getY(), parent.getZ() + Math.cos(angle) * 1.5, parent.getYRot(), parent.getXRot());
                serverLevel.sendParticles(ParticleTypes.PORTAL, livingSummon.getX(), livingSummon.getY(0.5), livingSummon.getZ(), 40, 1, 1, 1, 0);
                serverLevel.playSound(null, livingSummon.getX(), livingSummon.getY(), livingSummon.getZ(), 
                    net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
                serverLevel.addFreshEntity(livingSummon);
            }
        }
    }
}