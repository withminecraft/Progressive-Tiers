package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EnhancedMonster.MODID);

    public static final Map<String, RegistryObject<TraitItem>> TRAIT_ITEMS = new HashMap<>();

    static {
        for (String traitId : TraitConfig.getValidTraits()) {
        String registryName = traitId.toLowerCase(Locale.ROOT).replace("em_effect_", "trait_");
        
        TRAIT_ITEMS.put(traitId, ITEMS.register(registryName, 
            () -> new TraitItem(new Item.Properties().stacksTo(16), traitId)));
    }
    }

    public static ItemStack createTraitStack(String traitId, int level) {
        RegistryObject<TraitItem> itemObj = TRAIT_ITEMS.get(traitId);
        if (itemObj == null || !itemObj.isPresent()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(itemObj.get());
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt(TraitItem.TAG_TRAIT_LEVEL, level);
        nbt.putString(TraitItem.TAG_TRAIT_TYPE, traitId); 
        
        return stack;
    }
}