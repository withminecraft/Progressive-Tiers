package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.util.StructureValidator;
import net.Momo_EMT.enhanced_monster.capability.IMobTrait;
import net.Momo_EMT.enhanced_monster.capability.MobTraitProvider;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.Momo_EMT.enhanced_monster.special.SpecialManager;

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
    public static final String NBT_PREFIX = "EM_Effect_";
    public static final String POWERFUL = NBT_PREFIX + "powerful";
    public static final String REGENERATING = NBT_PREFIX + "regenerating";
    public static final String SPEEDY = NBT_PREFIX + "speedy";
    public static final String PROTECTED = NBT_PREFIX + "protected";
    public static final String FIRE_PROT = NBT_PREFIX + "fire_prot";
    public static final String POISONOUS = NBT_PREFIX + "poisonous";
    public static final String STRAY = NBT_PREFIX + "stray";
    public static final String WEAKENER = NBT_PREFIX + "weakener";
    public static final String BERSERK = NBT_PREFIX + "berserk";
    public static final String LIFESTEAL = NBT_PREFIX + "lifesteal";
    public static final String TANKY = NBT_PREFIX + "tanky";
    public static final String VOID = NBT_PREFIX + "void";
    public static final String SUMMONER = NBT_PREFIX + "summoner";
    public static final String WITHERING = NBT_PREFIX + "withering";

    public static void apply(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        if (entity.getPersistentData().contains("EM_SkipAllocation")) return;
        if (entity.getMaxHealth() < 16.0) return;

        String dimensionId = entity.level().dimension().location().toString();
        if (ModConfig.CACHED_DIMENSION_BLACKLIST.contains(dimensionId)) {
            return; 
        }

        var resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (resourceLocation == null) return;
        String entityId = resourceLocation.toString();

        boolean isMobBoss = ModConfig.CACHED_BOSS_LIST.contains(entityId);

        if (!isMobBoss) {
            if (!(entity instanceof Enemy) || !isAllowed(entity)) return;
        }

        entity.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
            if (cap.isProcessed()) return;

            int quality;
            int count;

            if (isMobBoss) {
                quality = 3;
                count = 5;
                giveEffects(entity, count, quality, true, cap);
            } 
            else if (isEntityInSpecialStructure(entity)) {
                quality = rollStructureQuality(); 
                count = getCountForQuality(quality);
                adjustHealth(entity, quality);
                if (count > 0) {
                    giveEffects(entity, count, quality, quality == 3, cap);
                }
            }
            else {
                double maxHealth = entity.getMaxHealth();
                int tier = 1;
                if (maxHealth > ModConfig.TIER_2_LIMIT.get()) tier = 3;
                else if (maxHealth > ModConfig.TIER_1_LIMIT.get()) tier = 2;
                quality = rollQuality(tier);
                count = getCountForQuality(quality);
                adjustHealth(entity, quality);
                if (count > 0) {
                    giveEffects(entity, count, quality, quality == 3, cap);
                }
            }

            cap.setQuality(quality);
            cap.setProcessed(true);
            cap.setBoss(isMobBoss);

            SpecialManager.tryApply(entity, quality);
            syncAndSave(entity, cap);
        });
    }

    private static boolean isEntityInSpecialStructure(LivingEntity entity) {
        return StructureValidator.isEntityInSpecialStructure(entity);
    }

    private static void syncAndSave(LivingEntity entity, IMobTrait cap) {
        CompoundTag persistent = entity.getPersistentData();
        persistent.putInt(TAG_QUALITY, cap.getQuality());
        persistent.putBoolean("IsBoss", cap.isBoss());

        CompoundTag syncTag = cap.serializeNBT();
        EnhancedMonster.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                new PacketSyncMobTrait(entity.getId(), syncTag));
    }

    private static void adjustHealth(LivingEntity entity, int quality) {
        if (isBoss(entity)) return;

        var attribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;

        attribute.removeModifier(HEALTH_MODIFIER_UUID);

        double cleanMax = attribute.getValue(); 
        double bonusHealth = 0;

        if (quality == 3) {
            double tier2Limit = ModConfig.TIER_2_LIMIT.get();
            if (cleanMax < tier2Limit) bonusHealth = tier2Limit - cleanMax;
        } 
        else if (quality == 2) {
            double tier1Limit = ModConfig.TIER_1_LIMIT.get();
            if (cleanMax < tier1Limit) bonusHealth = tier1Limit - cleanMax;
        }

        if (bonusHealth > 0) {
            attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, "EM Health Bonus", bonusHealth, AttributeModifier.Operation.ADDITION));
            
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private static int rollStructureQuality() {
        int roll = RANDOM.nextInt(100);
        if (roll < 10) return 1; 
        if (roll < 70) return 2; 
        return 3;    
    }

    private static int rollQuality(int tier) {
        int roll = RANDOM.nextInt(100);
        return switch (tier) {
            case 1 -> (roll < 84) ? 1 : (roll < 99 ? 2 : 3);
            case 2 -> (roll < 15) ? 1 : (roll < 90 ? 2 : 3);
            default -> (roll < 1) ? 1 : (roll < 25 ? 2 : 3);
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

    private static void giveEffects(LivingEntity entity, int count, int quality, boolean shouldGlow, IMobTrait cap) {
        List<EffectPools.EffectEntry> pool = new ArrayList<>(EffectPools.getPool(quality, isBoss(entity)));
        Collections.shuffle(pool);

        int applied = 0;
        for (EffectPools.EffectEntry entry : pool) {
            if (applied >= count) break;

            String effectTag = entry.tagName;
            int level = entry.level;

            boolean incompatible = cap.getTraits().keySet().stream().anyMatch(existing -> isIncompatible(effectTag, existing));
            if (incompatible) continue;

            cap.addTrait(effectTag, level);
            applyImmediateAttributes(entity, effectTag, level);
            applied++;
        }

        if (shouldGlow && ModConfig.ENABLE_GLOWING.get()) {
            entity.setGlowingTag(true);
        }
    }

    public static void applyImmediateAttributes(LivingEntity entity, String tag, int level) {
        if (tag.equals(POWERFUL)) {
            safeApplyModifier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_UUID, "EM Attack Bonus", (level + 1) * 2.0, AttributeModifier.Operation.ADDITION);
        } else if (tag.equals(SPEEDY)) {
            safeApplyModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID, "EM Speed Bonus", (level + 1) * 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
        } else if (tag.equals(TANKY)) {
            safeApplyModifier(entity, Attributes.ARMOR, ARMOR_MODIFIER_UUID, "EM Armor Bonus", (level + 1) * 4.0, AttributeModifier.Operation.ADDITION);
            safeApplyModifier(entity, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER_UUID, "EM Toughness Bonus", (level + 1) * 4.0, AttributeModifier.Operation.ADDITION);
            safeApplyModifier(entity, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_UUID, "EM Knockback Bonus", (level + 1) * 0.2, AttributeModifier.Operation.ADDITION);
        }
    }

    public static void removeAllAttributeModifiers(LivingEntity entity) {
        Map<net.minecraft.world.entity.ai.attributes.Attribute, UUID> modifiersToRemove = Map.of(
            Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_UUID,
            Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID,
            Attributes.ARMOR, ARMOR_MODIFIER_UUID,
            Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER_UUID,
            Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_UUID
        );

        modifiersToRemove.forEach((attr, uuid) -> {
            AttributeInstance instance = entity.getAttribute(attr);
            if (instance != null && instance.getModifier(uuid) != null) {
                instance.removeModifier(uuid);
            }
        });
    }

    private static void safeApplyModifier(LivingEntity entity, net.minecraft.world.entity.ai.attributes.Attribute attrType, UUID uuid, String name, double value, AttributeModifier.Operation op) {
        AttributeInstance instance = entity.getAttribute(attrType);
        if (instance != null) {
            instance.removeModifier(uuid);
            instance.addPermanentModifier(new AttributeModifier(uuid, name, value, op));
        }
    }

    private static boolean isAllowed(LivingEntity entity) {
        Class<?> entityClass = entity.getClass();
        String className = entityClass.getName();
        
        // 针对 Goety 模组的包名拦截逻辑
        if (className.startsWith("com.Polarice3.Goety.common.entities.")) {
            if (className.startsWith("com.Polarice3.Goety.common.entities.ally.") || 
                className.startsWith("com.Polarice3.Goety.common.entities.neutral.") || 
                className.startsWith("com.Polarice3.Goety.common.entities.hostile.servants.")) {
                return false;
            }
        }

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
        return resourceLocation != null && ModConfig.CACHED_BOSS_LIST.contains(resourceLocation.toString());
    }

    private static boolean isIncompatible(String trait1, String trait2) {
        if (trait1.equals(trait2)) return true;
        
        if (trait1.equals(PROTECTED) && trait2.equals(REGENERATING)) return true;
        if (trait1.equals(REGENERATING) && trait2.equals(PROTECTED)) return true;

        if (trait1.equals(VOID)) return trait2.equals(BERSERK) || trait2.equals(POWERFUL);
        if (trait1.equals(BERSERK) || trait1.equals(POWERFUL)) return trait2.equals(VOID);
        
        return false;
    }
}