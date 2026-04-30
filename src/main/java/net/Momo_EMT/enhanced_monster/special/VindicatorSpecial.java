package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class VindicatorSpecial implements ISpecialElite {
    public static final String TAG_DROP_EMERALD = "em_drop_emerald_block";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Vindicator vindicator)) return;

        var enchantments = vindicator.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ItemStack ironAxe = new ItemStack(Items.IRON_AXE);
        ironAxe.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 5);
        ironAxe.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        ironAxe.enchant(enchantments.getOrThrow(Enchantments.BINDING_CURSE), 1);
        ironAxe.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        vindicator.setItemSlot(EquipmentSlot.MAINHAND, ironAxe);

        ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
        vindicator.setItemSlot(EquipmentSlot.OFFHAND, totem);

        equipIronArmor(vindicator, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE, enchantments);
        equipIronArmor(vindicator, EquipmentSlot.LEGS, Items.IRON_LEGGINGS, enchantments);
        equipIronArmor(vindicator, EquipmentSlot.FEET, Items.IRON_BOOTS, enchantments);
        
        vindicator.getPersistentData().putBoolean(TAG_DROP_EMERALD, true);
    }

    private void equipIronArmor(Vindicator vindicator, EquipmentSlot slot, Item item, HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(enchantLookup.getOrThrow(Enchantments.PROTECTION), 4);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        vindicator.setItemSlot(slot, stack);
    }
}