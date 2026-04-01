package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = EnhancedMonster.MODID)
public class TraitUseHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        
        if (stack.getItem() instanceof TraitItem && event.getEntity().isShiftKeyDown()) {
            if (event.getTarget() instanceof LivingEntity target) {
                
                InteractionResult result = stack.getItem().interactLivingEntity(
                    stack, 
                    event.getEntity(), 
                    target, 
                    event.getHand()
                );
                
                if (result.consumesAction()) {
                    event.setCanceled(true);
                    event.setCancellationResult(result);
                }
            }
        }
    }
}