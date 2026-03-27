package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Optional;

public class PiglinBruteSpecial implements ISpecialElite {
    public static final String TAG_DROP_SCRAP = "em_drop_ancient_debris";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof PiglinBrute brute)) return;

        var enchantments = brute.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        equipArmor(brute, EquipmentSlot.HEAD, Items.NETHERITE_HELMET, enchantments);
        equipArmor(brute, EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE, enchantments);
        equipArmor(brute, EquipmentSlot.LEGS, Items.NETHERITE_LEGGINGS, enchantments);
        equipArmor(brute, EquipmentSlot.FEET, Items.NETHERITE_BOOTS, enchantments);

        ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
        axe.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        axe.enchant(enchantments.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        axe.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        brute.setItemSlot(EquipmentSlot.MAINHAND, axe);

        brute.getPersistentData().putBoolean(TAG_DROP_SCRAP, true);
    }

    private void equipArmor(PiglinBrute brute, EquipmentSlot slot, Item item, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(enchantLookup.getOrThrow(Enchantments.PROTECTION), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        applySnoutGoldTrim(brute, stack);
        
        brute.setItemSlot(slot, stack);
    }

    private void applySnoutGoldTrim(LivingEntity entity, ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        var registryAccess = entity.level().registryAccess();
        var patterns = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
        var materials = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);

        Optional<Holder.Reference<TrimPattern>> pattern = patterns.get(TrimPatterns.SNOUT);
        Optional<Holder.Reference<TrimMaterial>> material = materials.get(TrimMaterials.GOLD);

        if (pattern.isPresent() && material.isPresent()) {
            ArmorTrim trim = new ArmorTrim(material.get(), pattern.get());
            stack.set(DataComponents.TRIM, trim);
        }
    }
}