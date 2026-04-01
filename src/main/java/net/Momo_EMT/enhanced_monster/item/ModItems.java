package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, EnhancedMonster.MODID);

    public static final Map<String, DeferredHolder<Item, TraitItem>> TRAIT_ITEMS = new HashMap<>();

    static {
        for (String traitId : TraitConfig.getValidTraits()) {
            String registryName = traitId.toLowerCase(Locale.ROOT).replace("em_effect_", "trait_");

            TRAIT_ITEMS.put(traitId, ITEMS.register(registryName,
                    () -> new TraitItem(new Item.Properties().stacksTo(16), traitId)));
        }
    }

    public static ItemStack createTraitStack(String traitId, int level) {
        DeferredHolder<Item, TraitItem> itemObj = TRAIT_ITEMS.get(traitId);
        if (itemObj == null || !itemObj.isBound()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(itemObj.get());

        CompoundTag nbt = new CompoundTag();
        nbt.putInt(TraitItem.TAG_TRAIT_LEVEL, level);
        nbt.putString(TraitItem.TAG_TRAIT_TYPE, traitId);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        return stack;
    }
}