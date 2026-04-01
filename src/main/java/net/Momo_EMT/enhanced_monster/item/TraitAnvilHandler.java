package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.Momo_EMT.enhanced_monster.item.TraitItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnhancedMonster.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TraitAnvilHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof TraitItem itemL && right.getItem() instanceof TraitItem itemR) {
            if (itemL == itemR) {
                String type = itemL.getTraitType(); 
                int lvlL = left.getOrCreateTag().getInt("TraitLevel");
                int lvlR = right.getOrCreateTag().getInt("TraitLevel");

                if (lvlL == lvlR && lvlL < TraitConfig.getMaxLevel(type)) {
                    ItemStack output = new ItemStack(itemL);
                    output.getOrCreateTag().putInt("TraitLevel", lvlL + 1);
                    
                    event.setOutput(output);
                    event.setCost((lvlL + 1) * 1);
                }
            }
        }
    }
}