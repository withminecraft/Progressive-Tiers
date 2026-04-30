package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.core.component.DataComponents;

public class EvokerSpecial implements ISpecialElite {
    public static final String TAG_TOTEM_COUNT = "em_totem_resurrections"; 
    public static final String TAG_DROP_TOTEM = "em_drop_extra_totem";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Evoker evoker)) return;

        var enchantments = evoker.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        equipIronArmor(evoker, EquipmentSlot.CHEST, Items.IRON_CHESTPLATE, enchantments);
        equipIronArmor(evoker, EquipmentSlot.LEGS, Items.IRON_LEGGINGS, enchantments);
        equipIronArmor(evoker, EquipmentSlot.FEET, Items.IRON_BOOTS, enchantments);
        
        evoker.getPersistentData().putInt(TAG_TOTEM_COUNT, 5);   
        evoker.getPersistentData().putBoolean(TAG_DROP_TOTEM, true);
    }

    private void equipIronArmor(Evoker evoker, EquipmentSlot slot, Item item, HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(enchantLookup.getOrThrow(Enchantments.PROTECTION), 4);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        evoker.setItemSlot(slot, stack);
    }
}