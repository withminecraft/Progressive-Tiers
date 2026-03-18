package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class DrownedSpecial implements ISpecialElite {
    public static final String TAG_DROP_TRIDENT = "em_drop_special_trident";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Drowned drowned)) return;

        drowned.setCanPickUpLoot(false);

        equipArmor(drowned, EquipmentSlot.HEAD, Items.DIAMOND_HELMET);
        equipArmor(drowned, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE);
        equipArmor(drowned, EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS);
        equipArmor(drowned, EquipmentSlot.FEET, Items.DIAMOND_BOOTS);

        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.enchant(Enchantments.CHANNELING, 1);
        drowned.setItemSlot(EquipmentSlot.MAINHAND, trident);

        drowned.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        drowned.getPersistentData().putBoolean(TAG_DROP_TRIDENT, true);
    }

    private void equipArmor(Drowned drowned, EquipmentSlot slot, net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.THORNS, 3);
        drowned.setItemSlot(slot, stack);
        drowned.setDropChance(slot, 0.0F);
    }
}