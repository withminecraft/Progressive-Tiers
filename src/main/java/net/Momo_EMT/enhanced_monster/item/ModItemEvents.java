package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.Item;

@EventBusSubscriber(modid = EnhancedMonster.MODID)
public class ModItemEvents {

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            for (String traitId : TraitConfig.getValidTraits()) {
                DeferredHolder<Item, TraitItem> regObj = ModItems.TRAIT_ITEMS.get(traitId);
                
                if (regObj != null && regObj.isBound()) {
                    int maxLevel = TraitConfig.getMaxLevel(traitId);
                    
                    for (int lvl = 0; lvl <= maxLevel; lvl++) {
                        event.accept(ModItems.createTraitStack(traitId, lvl));
                    }
                }
            }
        }
    }
}