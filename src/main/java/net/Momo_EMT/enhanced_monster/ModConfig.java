package net.Momo_EMT.enhanced_monster;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "enhanced_monster", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELIST;
    public static final ForgeConfigSpec.BooleanValue IS_WHITELIST_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_LIST;

    public static final ForgeConfigSpec.DoubleValue TIER_1_LIMIT;
    public static final ForgeConfigSpec.DoubleValue TIER_2_LIMIT;
    
    public static final ForgeConfigSpec.BooleanValue ENABLE_DROPS;
    
    public static final ForgeConfigSpec.BooleanValue ENABLE_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GLOWING;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> QUALITY_2_EXTRA_DROPS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> QUALITY_3_EXTRA_DROPS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_EXTRA_DROPS;

    public static final Set<String> CACHED_BLACKLIST = new HashSet<>();
    public static final Set<String> CACHED_WHITELIST = new HashSet<>();
    public static final Set<String> CACHED_BOSS_LIST = new HashSet<>();

    static {
        BUILDER.push("General Settings");

        IS_WHITELIST_MODE = BUILDER.comment(
                "是否启用白名单模式（默认黑名单）。",
                "Whether to enable whitelist mode (Defaults to blacklist mode)."
        ).define("use_whitelist", false);
        
        BLACKLIST = BUILDER.comment(
                "黑名单：填入实体ID（如 minecraft:zombie）或 ModID（如 alexsmobs）。这些生物不会成为精英怪。",
                "Blacklist: Enter Entity IDs (e.g., minecraft:zombie) or Mod IDs (e.g., alexsmobs). These mobs will not become Elites."
        ).defineList("blacklist", List.of("minecraft:slime", "minecraft:silverfish", "minecraft:vex", "minecraft:ghast", "minecraft:magma_cube", "minecraft:endermite", "cataclysm:endermaptera", "cataclysm:lionfish", "cataclysm:urchinkin", "cataclysm:symbiocto"), obj -> obj instanceof String);

        WHITELIST = BUILDER.comment(
                "白名单：仅在此名单内的生物生效。",
                "Whitelist: Only entities in this list can become Elites."
        ).defineList("whitelist", Collections.emptyList(), obj -> obj instanceof String);

        BOSS_LIST = BUILDER.comment(
                "BOSS名单：百分百获得5个高级效果并发光。",
                "Boss List: Entities in this list will always receive 5 high-tier effects and the Glowing effect."
        ).defineList("boss_list", List.of("minecraft:ender_dragon", "minecraft:wither", "cataclysm:ender_guardian", "cataclysm:ignis", "cataclysm:netherite_monstrosity", "cataclysm:the_harbinger", "cataclysm:the_leviathan", "cataclysm:ancient_remnant", "cataclysm:maledictus", "cataclysm:scylla"), obj -> obj instanceof String);

        TIER_1_LIMIT = BUILDER.comment(
                "第一梯度上限血量（默认40）。血量超过此值的生物将被视为第二梯度。",
                "Tier 1 Max Health (Default 40). Entities with health above this will be considered Tier 2."
        ).defineInRange("tier_1_limit", 40.0, 0.0, 1000000.0);

        TIER_2_LIMIT = BUILDER.comment(
                "第二梯度上限血量（默认120）。血量超过此值的生物将被视为第三梯度。",
                "Tier 2 Max Health (Default 120). Entities with health above this will be considered Tier 3."
        ).defineInRange("tier_2_limit", 120.0, 0.0, 1000000.0);

        ENABLE_DROPS = BUILDER.comment(
                "是否开启精英怪/BOSS死亡时的随机附魔书掉落。",
                "Enable/Disable randomized enchanted book drops for Elite/Boss mobs."
        ).define("enable_drops", true);

        ENABLE_PARTICLES = BUILDER.comment(
                "是否开启精英怪/BOSS的粒子特效（仅视觉）。",
                "Enable/Disable visual particle effects for Elite/Boss mobs."
        ).define("enable_particles", true);

        ENABLE_GLOWING = BUILDER.comment(
                "是否开启精英怪/BOSS的自动发光效果。",
                "Enable/Disable automatic Glowing effect for Elite/Boss mobs."
        ).define("enable_glowing", false);

        BUILDER.push("Extra Drops");
        String dropExample = "格式: \"物品ID,最小数量,最大数量,掉落概率(0-1)\"。例如: \"minecraft:diamond,1,2,0.5\" 代表50%几率掉落1-2个钻石。";
        String dropExampleEn = "Format: \"item_id,min,max,chance(0-1)\". E.g., \"minecraft:diamond,1,2,0.5\" means 50% chance to drop 1-2 diamonds.";

        QUALITY_2_EXTRA_DROPS = BUILDER.comment("品级2 (Quality 2) 的额外掉落列表。", dropExample, dropExampleEn)
                .defineList("quality_2_extra_drops", Collections.emptyList(), obj -> obj instanceof String);

        QUALITY_3_EXTRA_DROPS = BUILDER.comment("品级3 (Quality 3) 的额外掉落列表。", dropExample, dropExampleEn)
                .defineList("quality_3_extra_drops", List.of("minecraft:gold_ingot,2,5,0.8"), obj -> obj instanceof String);

        BOSS_EXTRA_DROPS = BUILDER.comment("BOSS 品级的额外掉落列表。", dropExample, dropExampleEn)
                .defineList("boss_extra_drops", List.of("minecraft:netherite_scrap,1,1,1.0", "minecraft:diamond,2,5,0.8"), obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static void bakeConfig() {
        CACHED_BLACKLIST.clear();
        CACHED_BLACKLIST.addAll(BLACKLIST.get());

        CACHED_WHITELIST.clear();
        CACHED_WHITELIST.addAll(WHITELIST.get());

        CACHED_BOSS_LIST.clear();
        CACHED_BOSS_LIST.addAll(BOSS_LIST.get());
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            bakeConfig();
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            bakeConfig(); 
            ModEvents.clearEnchantmentCache();
        }
    }

    public static boolean isBoss(String entityId) {
        return CACHED_BOSS_LIST.contains(entityId);
    }
}