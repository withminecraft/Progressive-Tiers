package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EffectAllocator;
import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.Momo_EMT.enhanced_monster.capability.MobTraitAttachment;
import net.Momo_EMT.enhanced_monster.capability.MobTraitData;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = EnhancedMonster.MODID)
public class TraitUseHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        var player = event.getEntity();
        
        if (!player.isShiftKeyDown() || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        if (stack.is(Items.AMETHYST_SHARD)) {
            if (target instanceof OwnableEntity ownable && player.getUUID().equals(ownable.getOwnerUUID())) {
                
                if (target.level().isClientSide) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }

                MobTraitData data = target.getData(MobTraitAttachment.MOB_TRAIT);
                
                data.getTraits().clear(); 

                EffectAllocator.removeAllAttributeModifiers(target);

                PacketDistributor.sendToPlayersTrackingEntity(target, 
                    new PacketSyncMobTrait(target.getId(), data.serializeNBT()));

                player.displayClientMessage(Component.translatable("chat.enhanced_monster.clear_success")
                    .withStyle(ChatFormatting.GREEN), true);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }

        if (stack.getItem() instanceof TraitItem) {
            InteractionResult result = stack.getItem().interactLivingEntity(
                stack, player, target, event.getHand()
            );
            
            if (result.consumesAction()) {
                event.setCanceled(true);
                event.setCancellationResult(result);
            }
        }
    }
}