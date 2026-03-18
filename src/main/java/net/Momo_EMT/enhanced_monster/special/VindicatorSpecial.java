package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class VindicatorSpecial implements ISpecialElite {
    public static final String TAG_DROP_EMERALD = "em_drop_emerald_block";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Vindicator vindicator)) return;

        vindicator.setCanPickUpLoot(false);

        ItemStack ironAxe = new ItemStack(Items.IRON_AXE);
        ironAxe.enchant(Enchantments.SHARPNESS, 5);
        vindicator.setItemSlot(EquipmentSlot.MAINHAND, ironAxe);

        vindicator.setDropChance(EquipmentSlot.MAINHAND, 0.0F);

        vindicator.getPersistentData().putBoolean(TAG_DROP_EMERALD, true);
    }
}