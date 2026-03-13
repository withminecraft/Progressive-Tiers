package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.capability.IMobTrait;
import net.Momo_EMT.enhanced_monster.capability.MobTraitProvider;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait; 
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class EffectAllocator {
    private static final Random RANDOM = new Random();
    
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("e6b98e8a-3601-447a-8f64-460d2e85a567");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("7f4f6b8c-5d1e-4c7b-9f0a-2d3e4f5a6b7c");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-4a5b-bc6d-7e8f9a0b1c2d");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("f1e2d3c4-b5a6-4987-8765-43210fedcba9");
    private static final UUID TOUGHNESS_MODIFIER_UUID = UUID.fromString("d9c8b7a6-0543-4210-afed-cba987654321");
    private static final UUID KNOCKBACK_MODIFIER_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    public static final String TAG_QUALITY = "EnhancedMonsterQuality";
    public static final String TAG_PROCESSED = "EM_Processed";

    public static final String NBT_PREFIX = "EM_Effect_";
    public static final String POWERFUL = NBT_PREFIX + "powerful";       // 强力
    public static final String REGENERATING = NBT_PREFIX + "regenerating";      // 再生
    public static final String SPEEDY = NBT_PREFIX + "speedy";          // 神速
    public static final String PROTECTED = NBT_PREFIX + "protected";    // 保护
    public static final String FIRE_PROT = NBT_PREFIX + "fire_prot";    // 阻燃
    public static final String POISONOUS = NBT_PREFIX + "poisonous";    // 剧毒
    public static final String STRAY = NBT_PREFIX + "stray";            // 凝滞
    public static final String WEAKENER = NBT_PREFIX + "weakener";              // 衰竭
    public static final String BERSERK = NBT_PREFIX + "berserk";        // 狂暴
    public static final String LIFESTEAL = NBT_PREFIX + "lifesteal";    // 嗜血
    public static final String TANKY = NBT_PREFIX + "tanky";            // 重甲

    public static void apply(LivingEntity entity) {
        if (entity.level().isClientSide) return;

        var resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (resourceLocation == null) return;
        String entityId = resourceLocation.toString();

        boolean isMobBoss = ModConfig.CACHED_BOSS_LIST.contains(entityId);

        if (!isMobBoss) {
            if (!(entity instanceof Enemy)) return; 
            if (!isAllowed(entity)) return;         
        }

        entity.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
            if (cap.isProcessed()) return;
            
            boolean isUndead = entity.isInvertedHealAndHarm();
            int quality;
            int count;

            if (isMobBoss) {
                quality = 3;
                count = 5;
                giveEffects(entity, count, quality, isUndead, true, cap);
            } else {
                double maxHealth = entity.getMaxHealth();
                int tier = 1;
                if (maxHealth > ModConfig.TIER_2_LIMIT.get()) tier = 3;
                else if (maxHealth > ModConfig.TIER_1_LIMIT.get()) tier = 2;

                quality = rollQuality(tier);
                count = getCountForQuality(quality);

                adjustHealth(entity, quality);

                if (count > 0) {
                    giveEffects(entity, count, quality, isUndead, quality == 3, cap);
                }
            }

            cap.setQuality(quality);
            cap.setProcessed(true);
            cap.setBoss(isMobBoss);

            syncToPersistentData(entity, cap);
        });
    }

    private static void syncToPersistentData(LivingEntity entity, IMobTrait cap) {
        CompoundTag nbt = entity.getPersistentData();
        nbt.putInt(TAG_QUALITY, cap.getQuality());
        nbt.putBoolean("IsBoss", cap.isBoss());
        cap.getTraits().forEach(nbt::putInt);

        CompoundTag syncTag = cap.serializeNBT();
        EnhancedMonster.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), 
            new PacketSyncMobTrait(entity.getId(), syncTag));
    }

    private static void adjustHealth(LivingEntity entity, int quality) {
        if (isBoss(entity)) return;

        double currentMax = entity.getMaxHealth();
        double bonusHealth = 0;

        if (quality == 3) {
            double tier2Limit = ModConfig.TIER_2_LIMIT.get();
            if (currentMax < tier2Limit) {
                bonusHealth = tier2Limit - currentMax;
            }
        } 
        else if (quality == 2) {
            double tier1Limit = ModConfig.TIER_1_LIMIT.get();
            if (currentMax < tier1Limit) {
                bonusHealth = tier1Limit - currentMax;
            }
        }

        if (bonusHealth > 0) {
            var attribute = entity.getAttribute(Attributes.MAX_HEALTH);
            if (attribute != null) {
                attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, "EM Health Bonus", bonusHealth, AttributeModifier.Operation.ADDITION));
                entity.setHealth(entity.getMaxHealth());
            }
        }
    }

    private static int rollQuality(int tier) {
        int roll = RANDOM.nextInt(1000);
        return switch (tier) {
            case 1 -> (roll < 900) ? 1 : (roll < 995 ? 2 : 3);
            case 2 -> (roll < 500) ? 1 : (roll < 950 ? 2 : 3);
            default -> (roll < 100) ? 1 : (roll < 700 ? 2 : 3);
        };
    }

    private static int getCountForQuality(int quality) {
        int roll = RANDOM.nextInt(100);
        return switch (quality) {
            case 1 -> (roll < 70) ? 0 : 1;
            case 2 -> (roll < 70) ? 2 : 3;
            case 3 -> (roll < 70) ? 4 : 5;
            default -> 0;
        };
    }

    private static void giveEffects(LivingEntity entity, int count, int quality, boolean isUndead, boolean shouldGlow, IMobTrait cap) {
        List<EffectPools.EffectEntry> pool = EffectPools.getPool(quality, isBoss(entity));
        Collections.shuffle(pool); 

        int applied = 0;

        for (EffectPools.EffectEntry entry : pool) {
            if (applied >= count) break;
            
            String effectTag = entry.tagName;
            int level = entry.level; 

            cap.addTrait(effectTag, level);
            
            applyImmediateAttributes(entity, effectTag, level);
            applied++;
        }

        if (shouldGlow) {
            entity.setGlowingTag(true);
        }
    }

    private static void applyImmediateAttributes(LivingEntity entity, String tag, int level) {
        if (tag.equals(POWERFUL)) {
            var attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attr != null) {
                attr.addPermanentModifier(new AttributeModifier(DAMAGE_MODIFIER_UUID, "EM Attack Bonus", (level + 1) * 2.0, AttributeModifier.Operation.ADDITION));
            }
        }
        if (tag.equals(SPEEDY)) {
            var attr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attr != null) {
                attr.addPermanentModifier(new AttributeModifier(SPEED_MODIFIER_UUID, "EM Speed Bonus", (level + 1) * 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
        // --- 重甲 ---
        if (tag.equals(TANKY)) {
            var armor = entity.getAttribute(Attributes.ARMOR);
            if (armor != null) {
                armor.addPermanentModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "EM Armor Bonus", (level + 1) * 4.0, AttributeModifier.Operation.ADDITION));
            }
            
            var toughness = entity.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (toughness != null) {
                toughness.addPermanentModifier(new AttributeModifier(TOUGHNESS_MODIFIER_UUID, "EM Toughness Bonus", (level + 1) * 2.0, AttributeModifier.Operation.ADDITION));
            }
            
            var knockback = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (knockback != null) {
                knockback.addPermanentModifier(new AttributeModifier(KNOCKBACK_MODIFIER_UUID, "EM Knockback Bonus", (level + 1) * 0.2, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    private static boolean isAllowed(LivingEntity entity) {
        var resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (resourceLocation == null) return false;

        String entityId = resourceLocation.toString();
        String modId = resourceLocation.getNamespace();

        if (ModConfig.IS_WHITELIST_MODE.get()) {
            return ModConfig.CACHED_WHITELIST.contains(entityId) || ModConfig.CACHED_WHITELIST.contains(modId);
        }
        return !(ModConfig.CACHED_BLACKLIST.contains(entityId) || ModConfig.CACHED_BLACKLIST.contains(modId));
    }

    private static boolean isBoss(LivingEntity entity) {
        var resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (resourceLocation == null) return false;
        return ModConfig.CACHED_BOSS_LIST.contains(resourceLocation.toString());
    }
}