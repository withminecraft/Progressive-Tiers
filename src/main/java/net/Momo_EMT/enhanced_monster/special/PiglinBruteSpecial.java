package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PiglinBruteSpecial implements ISpecialElite {
    public static final String TAG_DROP_SCRAP = "em_drop_ancient_debris";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof PiglinBrute brute)) return;

        brute.setCanPickUpLoot(false);

        equipArmor(brute, EquipmentSlot.HEAD, Items.NETHERITE_HELMET);
        equipArmor(brute, EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE);
        equipArmor(brute, EquipmentSlot.LEGS, Items.NETHERITE_LEGGINGS);
        equipArmor(brute, EquipmentSlot.FEET, Items.NETHERITE_BOOTS);

        ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
        brute.setItemSlot(EquipmentSlot.MAINHAND, axe);
        
        brute.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        
        brute.getPersistentData().putBoolean(TAG_DROP_SCRAP, true);
    }

    private void equipArmor(PiglinBrute brute, EquipmentSlot slot, net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        
        applySnoutGoldTrim(stack);
        
        brute.setItemSlot(slot, stack);
        brute.setDropChance(slot, 0.0F);
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