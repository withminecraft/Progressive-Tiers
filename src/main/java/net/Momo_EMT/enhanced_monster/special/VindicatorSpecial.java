package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class VindicatorSpecial implements ISpecialElite {
    public static final String TAG_DROP_EMERALD = "em_drop_emerald_block";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Vindicator vindicator)) return;

        ItemStack ironAxe = new ItemStack(Items.IRON_AXE);
        ironAxe.enchant(Enchantments.SHARPNESS, 5);
        ironAxe.enchant(Enchantments.VANISHING_CURSE, 1);
        ironAxe.enchant(Enchantments.BINDING_CURSE, 1);
        ironAxe.getOrCreateTag().putBoolean("Unbreakable", true);
        vindicator.setItemSlot(EquipmentSlot.MAINHAND, ironAxe);

        ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
        vindicator.setItemSlot(EquipmentSlot.OFFHAND, totem);

        equipIronArmor(vindicator, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE);
        equipIronArmor(vindicator, EquipmentSlot.LEGS, Items.IRON_LEGGINGS);
        equipIronArmor(vindicator, EquipmentSlot.FEET, Items.IRON_BOOTS);

        vindicator.getPersistentData().putBoolean(TAG_DROP_EMERALD, true);
    }

    private void equipIronArmor(Vindicator vindicator, EquipmentSlot slot, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        stack.enchant(Enchantments.VANISHING_CURSE, 1);
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        
        vindicator.setItemSlot(slot, stack);
    }
}