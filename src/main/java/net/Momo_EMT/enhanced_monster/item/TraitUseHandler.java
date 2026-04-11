package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.Momo_EMT.enhanced_monster.EffectAllocator;
import net.Momo_EMT.enhanced_monster.capability.MobTraitProvider;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = EnhancedMonster.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

                target.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
                    cap.getTraits().clear(); 

                    EffectAllocator.removeAllAttributeModifiers(target);

                    CompoundTag syncTag = cap.serializeNBT();
                    EnhancedMonster.CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> target), 
                        new PacketSyncMobTrait(target.getId(), syncTag)
                    );

                    player.displayClientMessage(Component.translatable("chat.enhanced_monster.clear_success").withStyle(ChatFormatting.GREEN), true);
                });

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