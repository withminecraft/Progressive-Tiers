package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EffectAllocator;
import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.Momo_EMT.enhanced_monster.capability.MobTraitProvider;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TraitItem extends Item {
    private final String traitType; 

    public static final String TAG_TRAIT_LEVEL = "TraitLevel";
    public static final String TAG_TRAIT_TYPE = "TraitType"; 

    public TraitItem(Properties props, String traitType) {
        super(props);
        this.traitType = traitType;
    }

    public String getTraitType() {
        return this.traitType;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {      
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        
        if (target.level().isClientSide) return InteractionResult.CONSUME;

        int itemLevel = stack.getOrCreateTag().getInt(TAG_TRAIT_LEVEL);

        if (!(target instanceof OwnableEntity ownable) || !player.getUUID().equals(ownable.getOwnerUUID())) {
            player.displayClientMessage(Component.translatable("chat.enhanced_monster.not_your_pet").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        return target.getCapability(MobTraitProvider.MOB_TRAIT).map(cap -> {
            var traits = cap.getTraits();
            
            if (traits.containsKey(traitType)) {
                if (itemLevel <= traits.get(traitType)) {
                    player.displayClientMessage(Component.translatable("chat.enhanced_monster.level_too_low").withStyle(ChatFormatting.YELLOW), true);
                    return InteractionResult.FAIL;
                }
            } 
            else if (traits.size() >= 5) {
                player.displayClientMessage(Component.translatable("chat.enhanced_monster.max_traits_reached").withStyle(ChatFormatting.GOLD), true);
                return InteractionResult.FAIL;
            }

            cap.addTrait(traitType, itemLevel);
            cap.setProcessed(true); 
            
            EffectAllocator.applyImmediateAttributes(target, traitType, itemLevel);

            CompoundTag syncTag = cap.serializeNBT();
            EnhancedMonster.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> target), 
                new PacketSyncMobTrait(target.getId(), syncTag)
            );
            
            if (!player.getAbilities().instabuild) stack.shrink(1);
            
            player.displayClientMessage(Component.translatable("chat.enhanced_monster.apply_success", 
                    Component.translatable("trait.enhanced_monster." + traitType.replace("EM_Effect_", "")), (itemLevel + 1)), true);
            
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.FAIL);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(TAG_TRAIT_LEVEL)) {
            String typeDisplay = this.traitType.replace("EM_Effect_", "");
            int lvl = nbt.getInt(TAG_TRAIT_LEVEL) + 1;
            tooltip.add(Component.translatable("trait.enhanced_monster." + typeDisplay.toLowerCase()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("enchantment.level." + lvl).withStyle(ChatFormatting.AQUA)); 
            tooltip.add(Component.translatable("tooltip.enhanced_monster.shift_to_use").withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("tooltip.enhanced_monster.empty_trait").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}