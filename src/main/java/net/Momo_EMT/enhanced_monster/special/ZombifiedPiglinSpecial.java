package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class ZombifiedPiglinSpecial implements ISpecialElite {
    public static final String TAG_DROP_GOLD = "em_drop_zombie_piglin_gold";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof ZombifiedPiglin zPiglin)) return;

        zPiglin.setCanPickUpLoot(false);

        equipGoldArmor(zPiglin, EquipmentSlot.HEAD, Items.GOLDEN_HELMET);
        equipGoldArmor(zPiglin, EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE);
        equipGoldArmor(zPiglin, EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS);
        equipGoldArmor(zPiglin, EquipmentSlot.FEET, Items.GOLDEN_BOOTS);

        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 5);
        sword.enchant(Enchantments.FIRE_ASPECT, 2);
        zPiglin.setItemSlot(EquipmentSlot.MAINHAND, sword);

        zPiglin.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        zPiglin.getPersistentData().putBoolean(TAG_DROP_GOLD, true);
    }

    private void equipGoldArmor(ZombifiedPiglin zPiglin, EquipmentSlot slot, net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        zPiglin.setItemSlot(slot, stack);
        zPiglin.setDropChance(slot, 0.0F);
    }
}