package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnhancedMonster.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItemEvents {

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            for (String traitId : TraitConfig.getValidTraits()) {
                RegistryObject<TraitItem> regObj = ModItems.TRAIT_ITEMS.get(traitId);
                
                if (regObj != null && regObj.isPresent()) {
                    int maxLevel = TraitConfig.getMaxLevel(traitId);
                    for (int lvl = 0; lvl <= maxLevel; lvl++) {
                        event.accept(ModItems.createTraitStack(traitId, lvl));
                    }
                }
            }
        }
    }
}