package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class DrownedSpecial implements ISpecialElite {
    public static final String TAG_DROP_TRIDENT = "em_drop_special_trident";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Drowned drowned)) return;

        equipArmor(drowned, EquipmentSlot.HEAD, Items.DIAMOND_HELMET);
        equipArmor(drowned, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE);
        equipArmor(drowned, EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS);
        equipArmor(drowned, EquipmentSlot.FEET, Items.DIAMOND_BOOTS);

        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.enchant(Enchantments.VANISHING_CURSE, 1);
        trident.enchant(Enchantments.BINDING_CURSE, 1);
        trident.getOrCreateTag().putBoolean("Unbreakable", true);
        
        drowned.setItemSlot(EquipmentSlot.MAINHAND, trident);

        drowned.getPersistentData().putBoolean(TAG_DROP_TRIDENT, true);
    }

    private void equipArmor(Drowned drowned, EquipmentSlot slot, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        stack.enchant(Enchantments.VANISHING_CURSE, 1);
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        
        applyCoastCopperTrim(stack);
        drowned.setItemSlot(slot, stack);
    }

    private void applyCoastCopperTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag trimTag = new CompoundTag();

        trimTag.putString("pattern", "minecraft:coast");
        trimTag.putString("material", "minecraft:copper");

        nbt.put("Trim", trimTag);
    }
}