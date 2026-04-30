package net.Momo_EMT.enhanced_monster.item;

import net.Momo_EMT.enhanced_monster.EffectAllocator;
import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.Momo_EMT.enhanced_monster.capability.MobTraitAttachment;
import net.Momo_EMT.enhanced_monster.capability.MobTraitData;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

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

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        int itemLevel = tag.getInt(TAG_TRAIT_LEVEL);

        if (!(target instanceof OwnableEntity ownable) || !player.getUUID().equals(ownable.getOwnerUUID())) {
            player.displayClientMessage(Component.translatable("chat.enhanced_monster.not_your_pet").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        MobTraitData data = target.getData(MobTraitAttachment.MOB_TRAIT);
        Map<String, Integer> traits = data.getTraits();

        if (this.traitType.equals(EffectAllocator.PROTECTED) && traits.containsKey(EffectAllocator.ELUSIVE)) {
            player.displayClientMessage(Component.translatable("chat.enhanced_monster.conflict_elusive").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (this.traitType.equals(EffectAllocator.ELUSIVE) && traits.containsKey(EffectAllocator.PROTECTED)) {
            player.displayClientMessage(Component.translatable("chat.enhanced_monster.conflict_protected").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (traits.containsKey(traitType)) {
            if (itemLevel <= traits.get(traitType)) {
                player.displayClientMessage(Component.translatable("chat.enhanced_monster.level_too_low").withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.FAIL;
            }
        } 
        else if (traits.size() >= 6) {
            player.displayClientMessage(Component.translatable("chat.enhanced_monster.max_traits_reached").withStyle(ChatFormatting.GOLD), true);
            return InteractionResult.FAIL;
        }

        data.addTrait(traitType, itemLevel);
        data.setProcessed(true);

        EffectAllocator.applyImmediateAttributes(target, traitType, itemLevel);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new PacketSyncMobTrait(target.getId(), data.serializeNBT()));

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.displayClientMessage(Component.translatable("chat.enhanced_monster.apply_success",
                Component.translatable("trait.enhanced_monster." + traitType.replace("EM_Effect_", "")), (itemLevel + 1)), true);

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains(TAG_TRAIT_LEVEL)) {
            CompoundTag nbt = customData.copyTag();
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