package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        crossbow.enchant(Enchantments.QUICK_CHARGE, 3);
        crossbow.enchant(Enchantments.MULTISHOT, 1);
        crossbow.enchant(Enchantments.VANISHING_CURSE, 1);
        crossbow.enchant(Enchantments.BINDING_CURSE, 1);
        
        crossbow.getOrCreateTag().putBoolean("Unbreakable", true);
        
        pillager.setItemSlot(EquipmentSlot.MAINHAND, crossbow);

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag fireworkTag = firework.getOrCreateTagElement("Fireworks");
        fireworkTag.putByte("Flight", (byte) 1);
        ListTag explosions = new ListTag();
        for (int i = 0; i < 7; i++) {
            CompoundTag star = new CompoundTag();
            star.putByte("Type", (byte) 0);
            star.putIntArray("Colors", new int[]{1908001}); 
            explosions.add(star);
        }
        fireworkTag.put("Explosions", explosions);
        firework.setCount(64);
        
        pillager.setItemSlot(EquipmentSlot.OFFHAND, firework);
        pillager.setDropChance(EquipmentSlot.OFFHAND, 0.0F);

        pillager.getPersistentData().putBoolean(TAG_DROP_EMERALD, true);
    }
}