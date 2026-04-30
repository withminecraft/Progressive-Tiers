package net.Momo_EMT.enhanced_monster;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = "enhanced_monster")
public class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;
    public static final ModConfigSpec.BooleanValue IS_WHITELIST_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BOSS_LIST;

    public static final ModConfigSpec.DoubleValue TIER_1_LIMIT;
    public static final ModConfigSpec.DoubleValue TIER_2_LIMIT;
    
    public static final ModConfigSpec.BooleanValue ENABLE_PARTICLES;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> QUALITY_2_EXTRA_DROPS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> QUALITY_3_EXTRA_DROPS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BOSS_EXTRA_DROPS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DIMENSION_BLACKLIST;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_POWERFUL;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_REGENERATING;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_SPEEDY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_PROTECTED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_FIRE_PROT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_POISONOUS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_STRAY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_WEAKENER;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_BERSERK;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_LIFESTEAL;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_TANKY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_VOID;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_SUMMONER;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_WITHERING;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_EROSIVE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MANUAL_ELUSIVE;

    public static final ModConfigSpec.DoubleValue POWERFUL_DAMAGE_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue SPEEDY_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue TANKY_ARMOR_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue TANKY_TOUGHNESS_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue TANKY_KNOCKBACK_PER_LEVEL;

    public static double CACHED_POWERFUL_DAMAGE;
    public static double CACHED_SPEEDY_VALUE;
    public static double CACHED_TANKY_ARMOR;
    public static double CACHED_TANKY_TOUGHNESS;
    public static double CACHED_TANKY_KNOCKBACK;

    public static final Set<String> CACHED_BLACKLIST = new HashSet<>();
    public static final Set<String> CACHED_WHITELIST = new HashSet<>();
    public static final Set<String> CACHED_BOSS_LIST = new HashSet<>();
    public static final Set<String> CACHED_DIMENSION_BLACKLIST = new HashSet<>();

    public static final Map<String, Set<String>> TRAIT_MANUAL_MAP = new HashMap<>();

    static {
        BUILDER.push("General Settings");

        IS_WHITELIST_MODE = BUILDER.comment(
                "是否启用白名单模式（默认黑名单）。",
                "Whether to enable whitelist mode (Defaults to blacklist mode)."
        ).define("use_whitelist", false);
        
        BLACKLIST = BUILDER.comment(
                "黑名单：填入实体ID（如 minecraft:zombie）或 ModID（如 alexsmobs）。这些生物不会成为精英怪。",
                "Blacklist: Enter Entity IDs (e.g., minecraft:zombie) or Mod IDs (e.g., alexsmobs). These mobs will not become Elites."
        ).defineList("blacklist", List.of(
                "cataclysm:symbiocto", "irons_spellbooks:summoned_zombie", "irons_spellbooks:summoned_skeleton", "irons_spellbooks:summoned_polar_bear", "irons_spellbooks:summoned_sword", 
                "irons_spellbooks:summoned_claymore", "irons_spellbooks:summoned_rapier"
        ), obj -> obj instanceof String);

        DIMENSION_BLACKLIST = BUILDER.comment(
                "维度黑名单：填入维度ID（如 minecraft:the_nether）。在这些维度生成的生物不会被强化。",
                "Dimension Blacklist: Enter Dimension IDs (e.g., minecraft:the_nether). Mobs in these dimensions will not be enhanced."
        ).defineList("dimension_blacklist", Collections.emptyList(), obj -> obj instanceof String);

        WHITELIST = BUILDER.comment(
                "白名单：仅在此名单内的生物生效。",
                "Whitelist: Only entities in this list can become Elites."
        ).defineList("whitelist", Collections.emptyList(), obj -> obj instanceof String);

        BOSS_LIST = BUILDER.comment(
                "BOSS名单：百分百获得6个高级效果。",
                "Boss List: Entities in this list will always receive 6 high-tier effects."
        ).defineList("boss_list", List.of(
                "minecraft:ender_dragon", "minecraft:wither", "cataclysm:ender_guardian", "cataclysm:ignis", "cataclysm:netherite_monstrosity", "cataclysm:the_harbinger", 
                "cataclysm:the_leviathan", "cataclysm:ancient_remnant", "cataclysm:maledictus", "cataclysm:scylla", "irons_spellbooks:dead_king", "irons_spellbooks:fire_boss", 
                "mowziesmobs:ferrous_wroughtnaut", "mowziesmobs:frostmaw", "mowziesmobs:umvuthi"
        ), obj -> obj instanceof String);

        TIER_1_LIMIT = BUILDER.comment(
                "第一梯度上限血量（默认40）。血量超过此值的生物将被视为第二梯度。",
                "Tier 1 Max Health (Default 40). Entities with health above this will be considered Tier 2."
        ).defineInRange("tier_1_limit", 40.0, 0.0, 1000000.0);

        TIER_2_LIMIT = BUILDER.comment(
                "第二梯度上限血量（默认120）。血量超过此值的生物将被视为第三梯度。",
                "Tier 2 Max Health (Default 120). Entities with health above this will be considered Tier 3."
        ).defineInRange("tier_2_limit", 120.0, 0.0, 1000000.0);

        ENABLE_PARTICLES = BUILDER.comment(
                "是否开启精英怪/BOSS的粒子特效（仅视觉）。",
                "Enable/Disable visual particle effects for Elite/Boss mobs."
        ).define("enable_particles", true);

        BUILDER.push("Trait Values");
        BUILDER.comment("词条属性数值配置 (每级增加量)");

        POWERFUL_DAMAGE_PER_LEVEL = BUILDER.comment("超限 (Powerful): 每级增加的攻击力 (默认 2.0)")
                .defineInRange("powerful_damage_per_level", 2.0, 0.0, 1000.0);

        SPEEDY_PER_LEVEL = BUILDER.comment("极速 (Speedy): 每级增加的移动速度比例 (默认 0.2 代表 20%)")
                .defineInRange("speedy_per_level", 0.2, 0.0, 10.0);

        TANKY_ARMOR_PER_LEVEL = BUILDER.comment("重甲 (Tanky): 每级增加的护甲值 (默认 4.0)")
                .defineInRange("tanky_armor_per_level", 4.0, 0.0, 1000.0);

        TANKY_TOUGHNESS_PER_LEVEL = BUILDER.comment("重甲 (Tanky): 每级增加的护甲韧性 (默认 4.0)")
                .defineInRange("tanky_toughness_per_level", 4.0, 0.0, 1000.0);

        TANKY_KNOCKBACK_PER_LEVEL = BUILDER.comment("重甲 (Tanky): 每级增加的击退抗性 (默认 0.2)")
                .defineInRange("tanky_knockback_per_level", 0.2, 0.0, 1.0);
        
        BUILDER.pop();

        BUILDER.push("Manual Assignments");
        BUILDER.comment("定制BOSS：在此名单中的生物将固定获得对应词条的【最大等级】，并标记为BOSS，不再参与随机抽取。", 
                        "Custom BOSS: Entities in these lists will receive the MAX LEVEL of the trait, be marked as a BOSS, and skip random allocation.");

        MANUAL_POWERFUL = BUILDER.comment("超限 (Powerful)")
                .defineList("powerful_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_REGENERATING = BUILDER.comment("再生 (Regen)")
                .defineList("regenerating_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_SPEEDY = BUILDER.comment("极速 (Speedy)")
                .defineList("speedy_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_PROTECTED = BUILDER.comment("保护 (Protected)")
                .defineList("protected_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_FIRE_PROT = BUILDER.comment("阻燃 (Fireproof)")
                .defineList("fire_prot_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_POISONOUS = BUILDER.comment("剧毒 (Poisonous)")
                .defineList("poisonous_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_STRAY = BUILDER.comment("寒霜 (Stray)")
                .defineList("stray_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_WEAKENER = BUILDER.comment("衰竭 (Weakener)")
                .defineList("weakener_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_BERSERK = BUILDER.comment("狂暴 (Berserk)")
                .defineList("berserk_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_LIFESTEAL = BUILDER.comment("嗜血 (Lifesteal)")
                .defineList("lifesteal_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_TANKY = BUILDER.comment("重甲 (Tanky)")
                .defineList("tanky_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_VOID = BUILDER.comment("虚无 (Void)")
                .defineList("void_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_SUMMONER = BUILDER.comment("召唤 (Summoner)")
                .defineList("summoner_entities", Collections.emptyList(), obj -> obj instanceof String);
        
        MANUAL_WITHERING = BUILDER.comment("凋零 (Withering)")
                .defineList("withering_entities", Collections.emptyList(), obj -> obj instanceof String);

        MANUAL_EROSIVE = BUILDER.comment("侵蚀 (Erosive)")
                .defineList("erosive_entities", Collections.emptyList(), obj -> obj instanceof String);

        MANUAL_ELUSIVE = BUILDER.comment("神隐 (Elusive)")
                .defineList("elusive_entities", Collections.emptyList(), obj -> obj instanceof String);

        BUILDER.pop();

        BUILDER.push("Extra Drops");
        String dropExample = "格式: \"物品ID,最小数量,最大数量,掉落概率(0-1)\"。例如: \"minecraft:diamond,1,2,0.5\" 代表50%几率掉落1-2个钻石。";
        String dropExampleEn = "Format: \"item_id,min,max,chance(0-1)\". E.g., \"minecraft:diamond,1,2,0.5\" means 50% chance to drop 1-2 diamonds.";
        
        QUALITY_2_EXTRA_DROPS = BUILDER.comment("品级2的额外掉落列表。", dropExample)
                .defineList("quality_2_extra_drops", Collections.emptyList(), obj -> obj instanceof String);

        QUALITY_3_EXTRA_DROPS = BUILDER.comment("品级3的额外掉落列表。", dropExample)
                .defineList("quality_3_extra_drops", List.of("minecraft:golden_apple,2,3,1.0"), obj -> obj instanceof String);

        BOSS_EXTRA_DROPS = BUILDER.comment("BOSS 品级的额外掉落列表。", dropExample)
                .defineList("boss_extra_drops", List.of("minecraft:enchanted_golden_apple,2,3,1.0", "minecraft:diamond,4,9,0.8"), obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void bakeConfig() {
        CACHED_BLACKLIST.clear();
        CACHED_BLACKLIST.addAll(BLACKLIST.get());

        CACHED_WHITELIST.clear();
        CACHED_WHITELIST.addAll(WHITELIST.get());

        CACHED_BOSS_LIST.clear();
        CACHED_BOSS_LIST.addAll(BOSS_LIST.get());

        CACHED_DIMENSION_BLACKLIST.clear();
        CACHED_DIMENSION_BLACKLIST.addAll(DIMENSION_BLACKLIST.get());

        TRAIT_MANUAL_MAP.clear();
        putManualCache(EffectAllocator.POWERFUL, MANUAL_POWERFUL.get());
        putManualCache(EffectAllocator.REGENERATING, MANUAL_REGENERATING.get());
        putManualCache(EffectAllocator.SPEEDY, MANUAL_SPEEDY.get());
        putManualCache(EffectAllocator.PROTECTED, MANUAL_PROTECTED.get());
        putManualCache(EffectAllocator.FIRE_PROT, MANUAL_FIRE_PROT.get());
        putManualCache(EffectAllocator.POISONOUS, MANUAL_POISONOUS.get());
        putManualCache(EffectAllocator.STRAY, MANUAL_STRAY.get());
        putManualCache(EffectAllocator.WEAKENER, MANUAL_WEAKENER.get());
        putManualCache(EffectAllocator.BERSERK, MANUAL_BERSERK.get());
        putManualCache(EffectAllocator.LIFESTEAL, MANUAL_LIFESTEAL.get());
        putManualCache(EffectAllocator.TANKY, MANUAL_TANKY.get());
        putManualCache(EffectAllocator.VOID, MANUAL_VOID.get());
        putManualCache(EffectAllocator.SUMMONER, MANUAL_SUMMONER.get());
        putManualCache(EffectAllocator.WITHERING, MANUAL_WITHERING.get());
        putManualCache(EffectAllocator.EROSIVE, MANUAL_EROSIVE.get());
        putManualCache(EffectAllocator.ELUSIVE, MANUAL_ELUSIVE.get());

        CACHED_POWERFUL_DAMAGE = POWERFUL_DAMAGE_PER_LEVEL.get();
        CACHED_SPEEDY_VALUE = SPEEDY_PER_LEVEL.get();
        CACHED_TANKY_ARMOR = TANKY_ARMOR_PER_LEVEL.get();
        CACHED_TANKY_TOUGHNESS = TANKY_TOUGHNESS_PER_LEVEL.get();
        CACHED_TANKY_KNOCKBACK = TANKY_KNOCKBACK_PER_LEVEL.get();
    }

    private static void putManualCache(String traitTag, List<? extends String> entities) {
        if (!entities.isEmpty()) {
            TRAIT_MANUAL_MAP.put(traitTag, new HashSet<>(entities));
        }
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            bakeConfig();
        }
    }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            bakeConfig(); 
        }
    }

    public static boolean isBoss(String entityId) {
        return CACHED_BOSS_LIST.contains(entityId);
    }
}