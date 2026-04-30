package net.Momo_EMT.enhanced_monster.client;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.Momo_EMT.enhanced_monster.EffectAllocator;
import net.Momo_EMT.enhanced_monster.capability.MobTraitProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum EMJadeProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "info");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        accessor.getEntity().getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
            Map<String, Integer> traits = cap.getTraits();
            
            if (!traits.isEmpty()) {
                List<Component> entries = new ArrayList<>();
                
                traits.forEach((key, level) -> {
                    entries.add(buildStyledEntry(key, level));
                });

                for (int i = 0; i < entries.size(); i += 2) {
                    MutableComponent line = entries.get(i).copy(); 
                    
                    if (i + 1 < entries.size()) {
                        line.append(Component.literal("  ")).append(entries.get(i + 1));
                    }
                    
                    tooltip.add(line);
                }
            }
        });
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        Entity entity = accessor.getEntity();
        if (entity instanceof LivingEntity living) {
            living.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
                CompoundTag traitsTag = new CompoundTag();
                cap.getTraits().forEach(traitsTag::putInt);
                data.put("EM_Traits", traitsTag);
                
                data.putInt("EM_Quality", cap.getQuality());
            });
        }
    }

    private int getTraitColor(String tag) {
        return switch (tag) {
            case EffectAllocator.POWERFUL -> 0xFF5555;
            case EffectAllocator.REGENERATING -> 0xFF55FF;
            case EffectAllocator.SPEEDY -> 0x55FFFF;
            case EffectAllocator.PROTECTED -> 0xFFFF55;
            case EffectAllocator.FIRE_PROT -> 0xFFAA00;
            case EffectAllocator.POISONOUS -> 0x55FF55;
            case EffectAllocator.STRAY -> 0x00AEEF;
            case EffectAllocator.WEAKENER -> 0x555555;
            case EffectAllocator.BERSERK -> 0xAA0000;
            case EffectAllocator.LIFESTEAL -> 0xCC0000;
            case EffectAllocator.TANKY -> 0x999999;
            case EffectAllocator.VOID -> 0x7000FF;
            case EffectAllocator.SUMMONER -> 0x9370DB;
            case EffectAllocator.WITHERING -> 0x3E2723;
            case EffectAllocator.EROSIVE -> 0x4B5320;
            case EffectAllocator.ELUSIVE -> 0xF5F5F5;
            default -> 0xFFFFFF;
        };
    }

    private String toRoman(int n) {
        String[] roman = {"0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (n >= 0 && n < roman.length) ? roman[n] : String.valueOf(n);
    }

    private MutableComponent buildStyledEntry(String tag, int level) {
        String shortName = tag.replace(EffectAllocator.NBT_PREFIX, "");
        String translationKey = "trait.enhanced_monster." + shortName;
        
        String roman = toRoman(level + 1);
        
        return Component.translatable(translationKey)
                .append(Component.literal(" " + roman))
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(getTraitColor(tag))));
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}