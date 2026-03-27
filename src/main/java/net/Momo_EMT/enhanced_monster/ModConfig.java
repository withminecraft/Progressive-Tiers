package net.Momo_EMT.enhanced_monster;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    
    public static final ModConfigSpec.BooleanValue ENABLE_DROPS;
    
    public static final ModConfigSpec.BooleanValue ENABLE_PARTICLES;
    public static final ModConfigSpec.BooleanValue ENABLE_GLOWING;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> QUALITY_2_EXTRA_DROPS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> QUALITY_3_EXTRA_DROPS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BOSS_EXTRA_DROPS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DIMENSION_BLACKLIST;

    public static final Set<String> CACHED_BLACKLIST = new HashSet<>();
    public static final Set<String> CACHED_WHITELIST = new HashSet<>();
    public static final Set<String> CACHED_BOSS_LIST = new HashSet<>();
    public static final Set<String> CACHED_DIMENSION_BLACKLIST = new HashSet<>();

    static {
        BUILDER.push("General Settings");

        IS_WHITELIST_MODE = BUILDER.comment(
                "是否启用白名单模式（默认黑名单）。",
                "Whether to enable whitelist mode (Defaults to blacklist mode)."
        ).define("use_whitelist", false);
        
        BLACKLIST = BUILDER.comment(
                "黑名单：填入实体ID（如 minecraft:zombie）或 ModID（如 alexsmobs）。这些生物不会成为精英怪。",
                "Blacklist: Enter Entity IDs or Mod IDs. These mobs will not become Elites."
        ).defineList("blacklist", List.of(
                "cataclysm:symbiocto", "irons_spellbooks:summoned_zombie", "irons_spellbooks:summoned_skeleton", "irons_spellbooks:summoned_polar_bear", "irons_spellbooks:summoned_sword", 
                "irons_spellbooks:summoned_claymore", "irons_spellbooks:summoned_rapier"
        ), obj -> obj instanceof String);

        DIMENSION_BLACKLIST = BUILDER.comment(
                "维度黑名单：填入维度ID。在这些维度生成的生物不会被强化。",
                "Dimension Blacklist: Enter Dimension IDs. Mobs in these dimensions will not be enhanced."
        ).defineList("dimension_blacklist", Collections.emptyList(), obj -> obj instanceof String);

        WHITELIST = BUILDER.comment(
                "白名单：仅在此名单内的生物生效。",
                "Whitelist: Only entities in this list can become Elites."
        ).defineList("whitelist", Collections.emptyList(), obj -> obj instanceof String);

        BOSS_LIST = BUILDER.comment(
                "BOSS名单：百分百获得精英等级并发光。",
                "Boss List: Entities in this list will always be treated as Boss tier."
        ).defineList("boss_list", List.of(
                "minecraft:ender_dragon", "minecraft:wither", "cataclysm:ender_guardian", "cataclysm:ignis", "cataclysm:netherite_monstrosity", "cataclysm:the_harbinger", 
                "cataclysm:the_leviathan", "cataclysm:ancient_remnant", "cataclysm:maledictus", "cataclysm:scylla", "irons_spellbooks:dead_king", "irons_spellbooks:fire_boss", 
                "mowziesmobs:ferrous_wroughtnaut", "mowziesmobs:frostmaw", "mowziesmobs:umvuthi"
        ), obj -> obj instanceof String);

        TIER_1_LIMIT = BUILDER.comment("第一梯度上限血量（默认40）。").defineInRange("tier_1_limit", 40.0, 0.0, 1000000.0);
        TIER_2_LIMIT = BUILDER.comment("第二梯度上限血量（默认120）。").defineInRange("tier_2_limit", 120.0, 0.0, 1000000.0);

        ENABLE_DROPS = BUILDER.comment("是否开启精英怪死亡时的随机附魔书掉落。").define("enable_drops", true);
        ENABLE_PARTICLES = BUILDER.comment("是否开启视觉粒子特效。").define("enable_particles", true);
        ENABLE_GLOWING = BUILDER.comment("是否开启自动发光效果。").define("enable_glowing", false);

        BUILDER.push("Extra Drops");
        String dropExample = "格式: \"物品ID,最小数量,最大数量,掉落概率(0-1)\"。";
        
        QUALITY_2_EXTRA_DROPS = BUILDER.comment("品级2的额外掉落列表。", dropExample)
                .defineList("quality_2_extra_drops", Collections.emptyList(), obj -> obj instanceof String);

        QUALITY_3_EXTRA_DROPS = BUILDER.comment("品级3的额外掉落列表。", dropExample)
                .defineList("quality_3_extra_drops", List.of("minecraft:golden_apple,2,3,0.8"), obj -> obj instanceof String);

        BOSS_EXTRA_DROPS = BUILDER.comment("BOSS 品级的额外掉落列表。", dropExample)
                .defineList("boss_extra_drops", List.of("minecraft:netherite_scrap,1,2,1.0", "minecraft:diamond,4,9,0.8"), obj -> obj instanceof String);
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
            ModEvents.clearEnchantmentCache();
        }
    }

    public static boolean isBoss(String entityId) {
        return CACHED_BOSS_LIST.contains(entityId);
    }
}