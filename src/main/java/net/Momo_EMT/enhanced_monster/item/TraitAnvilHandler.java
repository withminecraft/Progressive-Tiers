package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

@EventBusSubscriber(modid = EnhancedMonster.MODID)
public class TraitAnvilHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof TraitItem itemL && right.getItem() instanceof TraitItem itemR) {
            if (itemL == itemR) {
                String type = itemL.getTraitType();

                int lvlL = getTraitLevel(left);
                int lvlR = getTraitLevel(right);

                if (lvlL == lvlR && lvlL < TraitConfig.getMaxLevel(type)) {
                    ItemStack output = new ItemStack(itemL);
                    
                    setTraitLevel(output, lvlL + 1);

                    event.setOutput(output);
                    event.setCost((lvlL + 1));
                    event.setMaterialCost(1);
                }
            }
        }
    }

    private static int getTraitLevel(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getInt(TraitItem.TAG_TRAIT_LEVEL);
        }
        return 0;
    }

    private static void setTraitLevel(ItemStack stack, int level) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        
        tag.putInt(TraitItem.TAG_TRAIT_LEVEL, level);
        if (stack.getItem() instanceof TraitItem traitItem) {
            tag.putString(TraitItem.TAG_TRAIT_TYPE, traitItem.getTraitType());
        }
        
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}