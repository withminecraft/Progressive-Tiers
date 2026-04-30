package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
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
import net.minecraft.core.component.DataComponents;

import java.util.Optional;

public class DrownedSpecial implements ISpecialElite {
    public static final String TAG_DROP_TRIDENT = "em_drop_special_trident";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Drowned drowned)) return;

        var enchantments = drowned.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        
        equipArmor(drowned, EquipmentSlot.HEAD, Items.DIAMOND_HELMET, enchantments);
        equipArmor(drowned, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE, enchantments);
        equipArmor(drowned, EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS, enchantments);
        equipArmor(drowned, EquipmentSlot.FEET, Items.DIAMOND_BOOTS, enchantments);

        ItemStack trident = new ItemStack(Items.TRIDENT);
        trident.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        trident.enchant(enchantments.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        trident.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        drowned.setItemSlot(EquipmentSlot.MAINHAND, trident);
        drowned.getPersistentData().putBoolean(TAG_DROP_TRIDENT, true);
    }

    private void equipArmor(Drowned drowned, EquipmentSlot slot, Item item, HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(enchantLookup.getOrThrow(Enchantments.PROTECTION), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        applyCoastCopperTrim(drowned, stack);
        
        drowned.setItemSlot(slot, stack);
    }

    private void applyCoastCopperTrim(LivingEntity entity, ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        var registryAccess = entity.level().registryAccess();
        var patterns = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
        var materials = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);

        Optional<Holder.Reference<TrimPattern>> pattern = patterns.get(TrimPatterns.COAST);
        Optional<Holder.Reference<TrimMaterial>> material = materials.get(TrimMaterials.COPPER);

        if (pattern.isPresent() && material.isPresent()) {
            ArmorTrim trim = new ArmorTrim(material.get(), pattern.get());
            stack.set(DataComponents.TRIM, trim);
        }
    }
}