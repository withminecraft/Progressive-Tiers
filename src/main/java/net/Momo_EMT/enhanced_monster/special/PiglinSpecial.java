package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
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

public class PiglinSpecial implements ISpecialElite {
    public static final String TAG_DROP_GOLD = "em_drop_piglin_gold";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Piglin piglin)) return;

        var enchantments = piglin.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        equipGoldArmor(piglin, EquipmentSlot.HEAD, Items.GOLDEN_HELMET, enchantments);
        equipGoldArmor(piglin, EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE, enchantments);
        equipGoldArmor(piglin, EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS, enchantments);
        equipGoldArmor(piglin, EquipmentSlot.FEET, Items.GOLDEN_BOOTS, enchantments);

        if (piglin.level().random.nextFloat() < 0.5F) {
            ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
            sword.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 7);
            sword.enchant(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 3);
            applyEliteStatus(sword, enchantments);
            piglin.setItemSlot(EquipmentSlot.MAINHAND, sword);
        } else {
            ItemStack crossbow = new ItemStack(Items.CROSSBOW);
            crossbow.enchant(enchantments.getOrThrow(Enchantments.QUICK_CHARGE), 3);
            crossbow.enchant(enchantments.getOrThrow(Enchantments.PIERCING), 4);
            applyEliteStatus(crossbow, enchantments);
            piglin.setItemSlot(EquipmentSlot.MAINHAND, crossbow);
        }

        piglin.getPersistentData().putBoolean(TAG_DROP_GOLD, true);
    }

    private void equipGoldArmor(Piglin piglin, EquipmentSlot slot, Item item, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.PROTECTION), 4);
        
        applyEliteStatus(stack, enchantLookup);
        
        applySnoutNetheriteTrim(piglin, stack);
        
        piglin.setItemSlot(slot, stack);
    }

    private void applyEliteStatus(ItemStack stack, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        stack.enchant(enchantLookup.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    private void applySnoutNetheriteTrim(LivingEntity entity, ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        var registryAccess = entity.level().registryAccess();
        var patterns = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
        var materials = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);

        Optional<Holder.Reference<TrimPattern>> pattern = patterns.get(TrimPatterns.SNOUT);
        Optional<Holder.Reference<TrimMaterial>> material = materials.get(TrimMaterials.NETHERITE);

        if (pattern.isPresent() && material.isPresent()) {
            ArmorTrim trim = new ArmorTrim(material.get(), pattern.get());
            stack.set(DataComponents.TRIM, trim);
        }
    }
}