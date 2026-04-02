package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.util.StructureValidator;
import net.Momo_EMT.enhanced_monster.capability.MobTraitAttachment;
import net.Momo_EMT.enhanced_monster.capability.MobTraitData; 
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.Momo_EMT.enhanced_monster.special.SpecialManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class EffectAllocator {
    private static final Random RANDOM = new Random();

    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "health_bonus");
    private static final ResourceLocation DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "damage_bonus");
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "speed_bonus");
    private static final ResourceLocation ARMOR_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "armor_bonus");
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "toughness_bonus");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "knockback_bonus");

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

    public static void apply(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        if (entity.getPersistentData().contains("EM_SkipAllocation")) return;
        if (entity.getMaxHealth() < 16.0) return;

        String dimensionId = entity.level().dimension().location().toString();
        if (ModConfig.CACHED_DIMENSION_BLACKLIST.contains(dimensionId)) return;

        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String entityId = entityKey.toString();

        boolean isMobBoss = ModConfig.CACHED_BOSS_LIST.contains(entityId);
        if (!isMobBoss) {
            if (!(entity instanceof Enemy) || !isAllowed(entity)) return;
        }

        MobTraitData data = entity.getData(MobTraitAttachment.MOB_TRAIT);
        if (data.isProcessed()) return;

        int quality;
        int count;

        if (isMobBoss) {
            quality = 3;
            count = 5;
            giveEffects(entity, count, quality, true, data);
        } 
        else if (isEntityInSpecialStructure(entity)) {
            quality = rollStructureQuality(); 
            count = getCountForQuality(quality);
            adjustHealth(entity, quality);
            if (count > 0) {
                giveEffects(entity, count, quality, quality == 3, data);
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
                giveEffects(entity, count, quality, quality == 3, data);
            }
        }

        data.setQuality(quality);
        data.setProcessed(true);
        data.setBoss(isMobBoss);

        SpecialManager.tryApply(entity, quality);
        syncAndSave(entity, data);
    }

    private static boolean isEntityInSpecialStructure(LivingEntity entity) {
        return StructureValidator.isEntityInSpecialStructure(entity);
    }

    private static void syncAndSave(LivingEntity entity, MobTraitData data) {
        CompoundTag persistent = entity.getPersistentData();
        persistent.putInt(TAG_QUALITY, data.getQuality());
        persistent.putBoolean("IsBoss", data.isBoss());

        PacketDistributor.sendToPlayersTrackingEntity(entity, new PacketSyncMobTrait(entity.getId(), data.serializeNBT()));
    }

    private static void adjustHealth(LivingEntity entity, int quality) {
        if (isBoss(entity)) return;

        AttributeInstance attribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;

        attribute.removeModifier(HEALTH_MODIFIER_ID);

        double cleanMax = attribute.getBaseValue(); 
        double bonusHealth = 0;

        if (quality == 3) {
            double tier2Limit = ModConfig.TIER_2_LIMIT.get();
            if (cleanMax < tier2Limit) bonusHealth = tier2Limit - cleanMax;
        } else if (quality == 2) {
            double tier1Limit = ModConfig.TIER_1_LIMIT.get();
            if (cleanMax < tier1Limit) bonusHealth = tier1Limit - cleanMax;
        }

        if (bonusHealth > 0) {
            attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, bonusHealth, AttributeModifier.Operation.ADD_VALUE));
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
            case 1 -> (roll < 90) ? 1 : (roll < 99 ? 2 : 3);
            case 2 -> (roll < 15) ? 1 : (roll < 90 ? 2 : 3);
            default -> (roll < 1) ? 1 : (roll < 34 ? 2 : 3);
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

    private static void giveEffects(LivingEntity entity, int count, int quality, boolean shouldGlow, MobTraitData data) {
        List<EffectPools.EffectEntry> pool = new ArrayList<>(EffectPools.getPool(quality, isBoss(entity)));
        Collections.shuffle(pool);

        int applied = 0;
        for (EffectPools.EffectEntry entry : pool) {
            if (applied >= count) break;

            String effectTag = entry.tagName;
            int level = entry.level;

            boolean incompatible = data.getTraits().keySet().stream().anyMatch(existing -> isIncompatible(effectTag, existing));
            if (incompatible) continue;

            data.addTrait(effectTag, level);
            applyImmediateAttributes(entity, effectTag, level);
            applied++;
        }

        if (shouldGlow && ModConfig.ENABLE_GLOWING.get()) {
            entity.setGlowingTag(true);
        }
    }

    public static void applyImmediateAttributes(LivingEntity entity, String tag, int level) {
        if (tag.equals(POWERFUL)) {
            safeApplyModifier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, (level + 1) * 2.0, AttributeModifier.Operation.ADD_VALUE);
        } else if (tag.equals(SPEEDY)) {
            safeApplyModifier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, (level + 1) * 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        } else if (tag.equals(TANKY)) {
            safeApplyModifier(entity, Attributes.ARMOR, ARMOR_MODIFIER_ID, (level + 1) * 4.0, AttributeModifier.Operation.ADD_VALUE);
            safeApplyModifier(entity, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER_ID, (level + 1) * 4.0, AttributeModifier.Operation.ADD_VALUE);
            safeApplyModifier(entity, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_ID, (level + 1) * 0.2, AttributeModifier.Operation.ADD_VALUE);
        }
    }

    private static void safeApplyModifier(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attrType, ResourceLocation id, double value, AttributeModifier.Operation op) {
        AttributeInstance instance = entity.getAttribute(attrType);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, value, op));
        }
    }

    private static boolean isAllowed(LivingEntity entity) {
        String className = entity.getClass().getName();

        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String entityId = entityKey.toString();
        String modId = entityKey.getNamespace();

        if (ModConfig.IS_WHITELIST_MODE.get()) {
            return ModConfig.CACHED_WHITELIST.contains(entityId) || ModConfig.CACHED_WHITELIST.contains(modId);
        }
        return !(ModConfig.CACHED_BLACKLIST.contains(entityId) || ModConfig.CACHED_BLACKLIST.contains(modId));
    }

    private static boolean isBoss(LivingEntity entity) {
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return entityKey != null && ModConfig.CACHED_BOSS_LIST.contains(entityKey.toString());
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