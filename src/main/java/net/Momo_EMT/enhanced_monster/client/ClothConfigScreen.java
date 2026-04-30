package net.Momo_EMT.enhanced_monster.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.Momo_EMT.enhanced_monster.ModConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class ClothConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.enhanced_monster.config"))
                .setSavingRunnable(() -> {
                    ModConfig.SPEC.save();
                    ModConfig.bakeConfig();
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.enhanced_monster.general"));

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.enhanced_monster.use_whitelist"), ModConfig.IS_WHITELIST_MODE.get())
                .setDefaultValue(false)
                .setSaveConsumer(ModConfig.IS_WHITELIST_MODE::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.enhanced_monster.enable_particles"), ModConfig.ENABLE_PARTICLES.get())
                .setDefaultValue(true)
                .setSaveConsumer(ModConfig.ENABLE_PARTICLES::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("option.enhanced_monster.tier_1_limit"), ModConfig.TIER_1_LIMIT.get())
                .setDefaultValue(40.0)
                .setMin(0.0)
                .setSaveConsumer(ModConfig.TIER_1_LIMIT::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("option.enhanced_monster.tier_2_limit"), ModConfig.TIER_2_LIMIT.get())
                .setDefaultValue(120.0)
                .setMin(0.0)
                .setSaveConsumer(ModConfig.TIER_2_LIMIT::set)
                .build());
        
        general.addEntry(entryBuilder.startDoubleField(Component.translatable("option.enhanced_monster.powerful_damage_per_level"), ModConfig.POWERFUL_DAMAGE_PER_LEVEL.get())
                .setDefaultValue(2.0)
                .setMin(0.0)
                .setSaveConsumer(ModConfig.POWERFUL_DAMAGE_PER_LEVEL::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("option.enhanced_monster.speedy_per_level"), ModConfig.SPEEDY_PER_LEVEL.get())
                .setDefaultValue(0.2)
                .setMin(0.0)
                .setSaveConsumer(ModConfig.SPEEDY_PER_LEVEL::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("option.enhanced_monster.tanky_armor_per_level"), ModConfig.TANKY_ARMOR_PER_LEVEL.get())
                .setDefaultValue(4.0)
                .setMin(0.0)
                .setSaveConsumer(ModConfig.TANKY_ARMOR_PER_LEVEL::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("option.enhanced_monster.tanky_toughness_per_level"), ModConfig.TANKY_TOUGHNESS_PER_LEVEL.get())
                .setDefaultValue(4.0)
                .setMin(0.0)
                .setSaveConsumer(ModConfig.TANKY_TOUGHNESS_PER_LEVEL::set)
                .build());

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("option.enhanced_monster.tanky_knockback_per_level"), ModConfig.TANKY_KNOCKBACK_PER_LEVEL.get())
                .setDefaultValue(0.2)
                .setMin(0.0)
                .setSaveConsumer(ModConfig.TANKY_KNOCKBACK_PER_LEVEL::set)
                .build());

        ConfigCategory lists = builder.getOrCreateCategory(Component.translatable("category.enhanced_monster.lists"));

        lists.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.blacklist"), new ArrayList<>(ModConfig.BLACKLIST.get()))
                .setSaveConsumer(ModConfig.BLACKLIST::set)
                .build());

        lists.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.whitelist"), new ArrayList<>(ModConfig.WHITELIST.get()))
                .setSaveConsumer(ModConfig.WHITELIST::set)
                .build());

        lists.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.boss_list"), new ArrayList<>(ModConfig.BOSS_LIST.get()))
                .setSaveConsumer(ModConfig.BOSS_LIST::set)
                .build());

        lists.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.dimension_blacklist"), new ArrayList<>(ModConfig.DIMENSION_BLACKLIST.get()))
                .setSaveConsumer(ModConfig.DIMENSION_BLACKLIST::set)
                .build());

        ConfigCategory manual = builder.getOrCreateCategory(Component.translatable("category.enhanced_monster.manual"));

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.powerful"), new ArrayList<>(ModConfig.MANUAL_POWERFUL.get()))
                .setSaveConsumer(ModConfig.MANUAL_POWERFUL::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.regenerating"), new ArrayList<>(ModConfig.MANUAL_REGENERATING.get()))
                .setSaveConsumer(ModConfig.MANUAL_REGENERATING::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.speedy"), new ArrayList<>(ModConfig.MANUAL_SPEEDY.get()))
                .setSaveConsumer(ModConfig.MANUAL_SPEEDY::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.protected"), new ArrayList<>(ModConfig.MANUAL_PROTECTED.get()))
                .setSaveConsumer(ModConfig.MANUAL_PROTECTED::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.fire_prot"), new ArrayList<>(ModConfig.MANUAL_FIRE_PROT.get()))
                .setSaveConsumer(ModConfig.MANUAL_FIRE_PROT::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.poisonous"), new ArrayList<>(ModConfig.MANUAL_POISONOUS.get()))
                .setSaveConsumer(ModConfig.MANUAL_POISONOUS::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.stray"), new ArrayList<>(ModConfig.MANUAL_STRAY.get()))
                .setSaveConsumer(ModConfig.MANUAL_STRAY::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.weakener"), new ArrayList<>(ModConfig.MANUAL_WEAKENER.get()))
                .setSaveConsumer(ModConfig.MANUAL_WEAKENER::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.berserk"), new ArrayList<>(ModConfig.MANUAL_BERSERK.get()))
                .setSaveConsumer(ModConfig.MANUAL_BERSERK::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.lifesteal"), new ArrayList<>(ModConfig.MANUAL_LIFESTEAL.get()))
                .setSaveConsumer(ModConfig.MANUAL_LIFESTEAL::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.tanky"), new ArrayList<>(ModConfig.MANUAL_TANKY.get()))
                .setSaveConsumer(ModConfig.MANUAL_TANKY::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.void"), new ArrayList<>(ModConfig.MANUAL_VOID.get()))
                .setSaveConsumer(ModConfig.MANUAL_VOID::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.summoner"), new ArrayList<>(ModConfig.MANUAL_SUMMONER.get()))
                .setSaveConsumer(ModConfig.MANUAL_SUMMONER::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.withering"), new ArrayList<>(ModConfig.MANUAL_WITHERING.get()))
                .setSaveConsumer(ModConfig.MANUAL_WITHERING::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.erosive"), new ArrayList<>(ModConfig.MANUAL_EROSIVE.get()))
                .setSaveConsumer(ModConfig.MANUAL_EROSIVE::set).build());

        manual.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.manual.elusive"), new ArrayList<>(ModConfig.MANUAL_ELUSIVE.get()))
                .setSaveConsumer(ModConfig.MANUAL_ELUSIVE::set).build());

        ConfigCategory drops = builder.getOrCreateCategory(Component.translatable("category.enhanced_monster.drops"));

        drops.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.quality_2_drops"), new ArrayList<>(ModConfig.QUALITY_2_EXTRA_DROPS.get()))
                .setSaveConsumer(ModConfig.QUALITY_2_EXTRA_DROPS::set)
                .build());

        drops.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.quality_3_drops"), new ArrayList<>(ModConfig.QUALITY_3_EXTRA_DROPS.get()))
                .setSaveConsumer(ModConfig.QUALITY_3_EXTRA_DROPS::set)
                .build());

        drops.addEntry(entryBuilder.startStrList(Component.translatable("option.enhanced_monster.boss_drops"), new ArrayList<>(ModConfig.BOSS_EXTRA_DROPS.get()))
                .setSaveConsumer(ModConfig.BOSS_EXTRA_DROPS::set)
                .build());

        return builder.build();
    }
}