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

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.enhanced_monster.enable_glowing"), ModConfig.ENABLE_GLOWING.get())
                .setDefaultValue(false)
                .setSaveConsumer(ModConfig.ENABLE_GLOWING::set)
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