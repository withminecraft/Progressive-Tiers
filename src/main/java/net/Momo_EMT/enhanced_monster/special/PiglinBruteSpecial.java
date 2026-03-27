package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class PiglinBruteSpecial implements ISpecialElite {
    public static final String TAG_DROP_SCRAP = "em_drop_ancient_debris";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof PiglinBrute brute)) return;

        equipArmor(brute, EquipmentSlot.HEAD, Items.NETHERITE_HELMET);
        equipArmor(brute, EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE);
        equipArmor(brute, EquipmentSlot.LEGS, Items.NETHERITE_LEGGINGS);
        equipArmor(brute, EquipmentSlot.FEET, Items.NETHERITE_BOOTS);

        ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
        axe.enchant(Enchantments.VANISHING_CURSE, 1);
        axe.enchant(Enchantments.BINDING_CURSE, 1);
        axe.getOrCreateTag().putBoolean("Unbreakable", true);
        
        brute.setItemSlot(EquipmentSlot.MAINHAND, axe);
        
        brute.getPersistentData().putBoolean(TAG_DROP_SCRAP, true);
    }

    private void equipArmor(PiglinBrute brute, EquipmentSlot slot, net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        stack.enchant(Enchantments.VANISHING_CURSE, 1);
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        
        applySnoutGoldTrim(stack);
        
        brute.setItemSlot(slot, stack);
    }

    private void applySnoutGoldTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag trimTag = new CompoundTag();

        trimTag.putString("pattern", "minecraft:snout");
        trimTag.putString("material", "minecraft:gold");

        nbt.put("Trim", trimTag);
    }
}