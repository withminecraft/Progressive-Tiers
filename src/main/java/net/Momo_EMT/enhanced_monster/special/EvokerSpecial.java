package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class EvokerSpecial implements ISpecialElite {
    public static final String TAG_TOTEM_COUNT = "em_totem_resurrections";
    public static final String TAG_DROP_TOTEM = "em_drop_extra_totem";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Evoker evoker)) return;

        equipIronArmor(evoker, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE);
        equipIronArmor(evoker, EquipmentSlot.LEGS, Items.IRON_LEGGINGS);
        equipIronArmor(evoker, EquipmentSlot.FEET, Items.IRON_BOOTS);

        evoker.getPersistentData().putInt(TAG_TOTEM_COUNT, 5);
        evoker.getPersistentData().putBoolean(TAG_DROP_TOTEM, true);
    }

    private void equipIronArmor(Evoker evoker, EquipmentSlot slot, Item item) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        stack.enchant(Enchantments.VANISHING_CURSE, 1);
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        
        evoker.setItemSlot(slot, stack);
    }
}