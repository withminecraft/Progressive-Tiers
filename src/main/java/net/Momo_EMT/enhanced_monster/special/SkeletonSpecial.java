package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.Skeleton;
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

public class SkeletonSpecial implements ISpecialElite {
    public static final String TAG_REDIRECT = "em_redirect_damage";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Skeleton skeleton)) return;

        var enchantments = skeleton.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        SkeletonHorse horse = EntityType.SKELETON_HORSE.create(skeleton.level());
        if (horse != null) {
            horse.moveTo(skeleton.getX(), skeleton.getY(), skeleton.getZ(), skeleton.getYRot(), skeleton.getXRot());
            
            var speedAttr = horse.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.setBaseValue(speedAttr.getBaseValue() * 2.0D);
            }

            var kbAttr = horse.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kbAttr != null) {
                kbAttr.setBaseValue(1.0D);
            }

            horse.setTamed(true);
            horse.getPersistentData().putBoolean(TAG_REDIRECT, true);

            horse.getPersistentData().putLong(SpecialHorseHandler.TAG_SPAWN_TICK, skeleton.level().getGameTime());

            skeleton.level().addFreshEntity(horse);
            skeleton.startRiding(horse);
        }

        equipArmor(skeleton, EquipmentSlot.HEAD, Items.DIAMOND_HELMET, enchantments);
        equipArmor(skeleton, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE, enchantments);
        equipArmor(skeleton, EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS, enchantments);
        equipArmor(skeleton, EquipmentSlot.FEET, Items.DIAMOND_BOOTS, enchantments);

        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(enchantments.getOrThrow(Enchantments.POWER), 3);
        bow.enchant(enchantments.getOrThrow(Enchantments.PUNCH), 2);
        bow.enchant(enchantments.getOrThrow(Enchantments.FLAME), 1);
        bow.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        bow.enchant(enchantments.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        bow.set(DataComponents.UNBREAKABLE, new Unbreakable(true));

        skeleton.setItemSlot(EquipmentSlot.MAINHAND, bow);
    }

    private void equipArmor(Skeleton skeleton, EquipmentSlot slot, Item item, HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(enchantLookup.getOrThrow(Enchantments.PROTECTION), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        applyShaperRedstoneTrim(skeleton, stack);
        
        skeleton.setItemSlot(slot, stack);
    }

    private void applyShaperRedstoneTrim(LivingEntity entity, ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        var registryAccess = entity.level().registryAccess();
        var patterns = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
        var materials = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);

        Optional<Holder.Reference<TrimPattern>> pattern = patterns.get(TrimPatterns.SHAPER);
        Optional<Holder.Reference<TrimMaterial>> material = materials.get(TrimMaterials.REDSTONE);

        if (pattern.isPresent() && material.isPresent()) {
            ArmorTrim trim = new ArmorTrim(material.get(), pattern.get());
            stack.set(DataComponents.TRIM, trim);
        }
    }
}