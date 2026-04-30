package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.HolderLookup;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.List;

public class PillagerSpecial implements ISpecialElite {
    public static final String TAG_DROP_EMERALD = "em_drop_pillager_emerald";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Pillager pillager)) return;

        var enchantments = pillager.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        crossbow.enchant(enchantments.getOrThrow(Enchantments.QUICK_CHARGE), 3);
        crossbow.enchant(enchantments.getOrThrow(Enchantments.MULTISHOT), 1);
        crossbow.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        crossbow.enchant(enchantments.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        crossbow.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        pillager.setItemSlot(EquipmentSlot.MAINHAND, crossbow);

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET, 64);    
        List<FireworkExplosion> explosions = new ArrayList<>();
        IntList colors = new IntArrayList(new int[]{1908001});
        
        for (int i = 0; i < 7; i++) {
            explosions.add(new FireworkExplosion(FireworkExplosion.Shape.SMALL_BALL, colors, IntList.of(), false, false));
        }
        
        firework.set(DataComponents.FIREWORKS, new Fireworks(1, explosions));
        pillager.setItemSlot(EquipmentSlot.OFFHAND, firework);
        pillager.setDropChance(EquipmentSlot.OFFHAND, 0.0F);

        equipIronArmor(pillager, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE, enchantments);
        equipIronArmor(pillager, EquipmentSlot.LEGS, Items.IRON_LEGGINGS, enchantments);
        equipIronArmor(pillager, EquipmentSlot.FEET, Items.IRON_BOOTS, enchantments);

        pillager.getPersistentData().putBoolean(TAG_DROP_EMERALD, true);
    }

    private void equipIronArmor(Pillager pillager, EquipmentSlot slot, Item item, HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(enchantLookup.getOrThrow(Enchantments.PROTECTION), 4);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        pillager.setItemSlot(slot, stack);
    }
}