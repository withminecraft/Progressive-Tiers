package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnhancedMonster.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TraitUseHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        
        if (stack.getItem() instanceof TraitItem && event.getEntity().isShiftKeyDown()) {
            if (event.getTarget() instanceof LivingEntity target) {
                InteractionResult result = stack.getItem().interactLivingEntity(
                    stack, event.getEntity(), target, event.getHand()
                );
                
                if (result.consumesAction()) {
                    event.setCanceled(true);
                    event.setCancellationResult(result);
                }
            }
        }
    }
}