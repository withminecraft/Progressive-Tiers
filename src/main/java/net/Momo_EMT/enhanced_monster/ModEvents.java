package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.capability.MobTraitAttachment;
import net.Momo_EMT.enhanced_monster.capability.MobTraitData;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = "enhanced_monster")
public class ModEvents {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living && !living.level().isClientSide) {
            MobTraitData data = living.getData(MobTraitAttachment.MOB_TRAIT);
            if (data.isProcessed() && data.getQuality() > 0) {
                CompoundTag syncTag = data.serializeNBT(); 
                PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), 
                    new PacketSyncMobTrait(living.getId(), syncTag));
            }
        }
    }

    private static List<Holder<Enchantment>> CACHED_ENCHANTMENTS;

    public static void clearEnchantmentCache() {
        CACHED_ENCHANTMENTS = null;
    }

    private static List<Holder<Enchantment>> getAvailableEnchantments(LivingEntity entity) {
        if (CACHED_ENCHANTMENTS == null) {
            var registry = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            CACHED_ENCHANTMENTS = registry.listElements()
                .filter(ench -> !ench.is(EnchantmentTags.CURSE) && !ench.is(EnchantmentTags.TREASURE))
                .collect(Collectors.toList());
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
        if (entity.getPersistentData().getBoolean("em_loot_generated")) return;
        entity.getPersistentData().putBoolean("em_loot_generated", true);

        MobTraitData data = entity.getData(MobTraitAttachment.MOB_TRAIT);
        int quality = data.getQuality();
        boolean isBoss = data.isBoss();
        if (quality < 2 && !isBoss) return;

        if (isBoss) handleCustomDrops(entity, ModConfig.BOSS_EXTRA_DROPS.get());
        else if (quality == 3) handleCustomDrops(entity, ModConfig.QUALITY_3_EXTRA_DROPS.get());
        else if (quality == 2) handleCustomDrops(entity, ModConfig.QUALITY_2_EXTRA_DROPS.get());

        if (!ModConfig.ENABLE_DROPS.get()) return;

        RandomSource random = entity.getRandom();
        int dropCount = isBoss ? 3 : (quality == 3 ? 1 + random.nextInt(2) : (random.nextFloat() < 0.5f ? 1 : 0));
        if (dropCount <= 0) return;

        List<Holder<Enchantment>> available = getAvailableEnchantments(entity);
        if (available.isEmpty()) return;

        for (int i = 0; i < dropCount; i++) {
            Holder<Enchantment> randomEnch;
            Enchantment ench;
            int maxLvl;
            
            // --- 过滤逻辑开始 ---
            int attempts = 0;
            do {
                randomEnch = available.get(random.nextInt(available.size()));
                ench = randomEnch.value();
                maxLvl = ench.getMaxLevel();
                attempts++;
            } while (quality == 2 && !isBoss && maxLvl <= 1 && attempts < 10);
            // --- 过滤逻辑结束 ---

            int level;
            if (isBoss) {
                level = Math.max(1, maxLvl - 1);
            } else if (quality == 3) {
                int min = Math.max(1, maxLvl / 2);
                level = min + (maxLvl > min ? random.nextInt(maxLvl - min + 1) : 0);
            } else {
                level = 1 + random.nextInt(Math.max(1, maxLvl / 2));
            }

            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantments.Mutable mutableEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutableEnchants.set(randomEnch, level);
            EnchantmentHelper.setEnchantments(enchantedBook, mutableEnchants.toImmutable());

            ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), enchantedBook);
            itemEntity.setPickUpDelay(10);
            itemEntity.setInvulnerable(true);
            entity.level().addFreshEntity(itemEntity);
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

            MobTraitData atkData = attacker.getData(MobTraitAttachment.MOB_TRAIT);
            if (atkData.getTraits().containsKey(EffectAllocator.BERSERK)) {
                event.setAmount(event.getAmount() * 1.5f);
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.5), victim.getZ(), 10, 0.1, 0.1, 0.1, 0.5);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        MobTraitData vicData = victim.getData(MobTraitAttachment.MOB_TRAIT);
        Map<String, Integer> traits = vicData.getTraits();
        
        if (traits.containsKey(EffectAllocator.PROTECTED)) {
            int level = traits.get(EffectAllocator.PROTECTED) + 1;
            float reduction = Math.max(0.1f, 1.0f - (level * 0.1f));
            event.setNewDamage(event.getNewDamage() * reduction);
        }

        LivingEntity attacker = getAttacker(event.getSource().getEntity(), event.getSource().getDirectEntity());
        if (attacker != null && attacker.isAlive()) {
            MobTraitData atkData = attacker.getData(MobTraitAttachment.MOB_TRAIT);
            Map<String, Integer> atkTraits = atkData.getTraits();

            if (atkTraits.containsKey(EffectAllocator.LIFESTEAL) && attacker.getHealth() < attacker.getMaxHealth()) {
                float healAmount = Math.min(event.getNewDamage() * 0.5f, 12.0f);
                attacker.heal(healAmount);
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART, attacker.getX(), attacker.getY(0.5), attacker.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
                }
            }

            if (atkTraits.containsKey(EffectAllocator.VOID)) {
                int level = atkTraits.get(EffectAllocator.VOID) + 1;
                event.setNewDamage(event.getNewDamage() + (victim.getMaxHealth() * (level * 0.04f)));
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, victim.getX(), victim.getY(0.5), victim.getZ(), 10, 0.5, 0.5, 0.5, 0);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0 || event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        LivingEntity attacker = getAttacker(event.getSource().getEntity(), event.getSource().getDirectEntity());

        if (attacker != null && attacker.isAlive()) {
            MobTraitData atkData = attacker.getData(MobTraitAttachment.MOB_TRAIT);
            Map<String, Integer> atkTraits = atkData.getTraits();

            if (atkTraits.containsKey(EffectAllocator.POISONOUS)) {
                int amp = atkTraits.get(EffectAllocator.POISONOUS);
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 200, amp));
                if (amp >= 2) victim.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, amp - 2));
            }

            if (atkTraits.containsKey(EffectAllocator.STRAY)) {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, atkTraits.get(EffectAllocator.STRAY)));
            }

            if (atkTraits.containsKey(EffectAllocator.WEAKENER)) {
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, atkTraits.get(EffectAllocator.WEAKENER)));
            }
        }
    }

    private static LivingEntity getAttacker(Entity sourceEntity, Entity directEntity) {
        if (sourceEntity instanceof LivingEntity e) return e;
        if (directEntity instanceof Projectile p && p.getOwner() instanceof LivingEntity e) return e;
        return null;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

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
            MobTraitData data = entity.getData(MobTraitAttachment.MOB_TRAIT);
            
            Integer regenLevel = data.getTraits().get(EffectAllocator.REGENERATING);
            if (regenLevel != null && entity.getHealth() < entity.getMaxHealth()) {
                entity.heal((regenLevel + 1) * 0.5f);
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

    private static void spawnSummons(LivingEntity parent) {
        if (!(parent.level() instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 2; i++) {
            Entity summon = parent.getType().create(serverLevel);
            if (summon instanceof LivingEntity livingSummon) {
                livingSummon.getPersistentData().putBoolean("EM_SkipAllocation", true);
                livingSummon.getPersistentData().putUUID("EM_Summoner_Owner", parent.getUUID());
                if (parent instanceof Mob parentMob && livingSummon instanceof Mob summonMob) {
                    summonMob.setTarget(parentMob.getTarget());
                    summonMob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(summonMob.blockPosition()), 
                        net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED, null);
                }
                double angle = (parent.getYRot() + (i == 0 ? 135.0F : -135.0F)) * (Math.PI / 180.0D);
                livingSummon.moveTo(parent.getX() - Math.sin(angle) * 1.5, parent.getY(), parent.getZ() + Math.cos(angle) * 1.5, parent.getYRot(), parent.getXRot());
                serverLevel.sendParticles(ParticleTypes.PORTAL, livingSummon.getX(), livingSummon.getY(0.5), livingSummon.getZ(), 40, 0.2, 0.5, 0.2, 0.5);
                serverLevel.addFreshEntity(livingSummon);
            }
        }
    }
}