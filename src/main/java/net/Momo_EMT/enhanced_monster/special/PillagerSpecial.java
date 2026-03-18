package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class PillagerSpecial implements ISpecialElite {
    public static final String TAG_DROP_EMERALD = "em_drop_pillager_emerald";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Pillager pillager)) return;

        pillager.setCanPickUpLoot(false);

        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        crossbow.enchant(Enchantments.QUICK_CHARGE, 3);
        crossbow.enchant(Enchantments.PIERCING, 4);
        pillager.setItemSlot(EquipmentSlot.MAINHAND, crossbow);

        pillager.setDropChance(EquipmentSlot.MAINHAND, 0.0F);

        pillager.getPersistentData().putBoolean(TAG_DROP_EMERALD, true);
    }
}