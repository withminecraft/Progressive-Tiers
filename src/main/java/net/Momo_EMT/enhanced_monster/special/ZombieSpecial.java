package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Zombie;
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

public class ZombieSpecial implements ISpecialElite {
    public static final String TAG_REDIRECT = "em_redirect_damage";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Zombie zombie)) return;

        var enchantments = zombie.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        ZombieHorse horse = EntityType.ZOMBIE_HORSE.create(zombie.level());
        if (horse != null) {
            horse.moveTo(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYRot(), zombie.getXRot());
            
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

            horse.getPersistentData().putLong(SpecialHorseHandler.TAG_SPAWN_TICK, zombie.level().getGameTime());

            zombie.level().addFreshEntity(horse);
            zombie.startRiding(horse);
        }

        equip(zombie, EquipmentSlot.HEAD, Items.DIAMOND_HELMET, enchantments);
        equip(zombie, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE, enchantments);
        equip(zombie, EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS, enchantments);
        equip(zombie, EquipmentSlot.FEET, Items.DIAMOND_BOOTS, enchantments);

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(enchantments.getOrThrow(Enchantments.KNOCKBACK), 2);
        sword.enchant(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 2);
        sword.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 3);
        sword.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        sword.enchant(enchantments.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        sword.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        zombie.setItemSlot(EquipmentSlot.MAINHAND, sword);
    }

    private void equip(Zombie zombie, EquipmentSlot slot, Item item, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchantLookup) {
        ItemStack stack = new ItemStack(item);
        
        stack.enchant(enchantLookup.getOrThrow(Enchantments.PROTECTION), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchantLookup.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        
        applyWildAmethystTrim(zombie, stack);
        
        zombie.setItemSlot(slot, stack);
    }

    private void applyWildAmethystTrim(LivingEntity entity, ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        var registryAccess = entity.level().registryAccess();
        var patterns = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
        var materials = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);

        Optional<Holder.Reference<TrimPattern>> pattern = patterns.get(TrimPatterns.WILD);
        Optional<Holder.Reference<TrimMaterial>> material = materials.get(TrimMaterials.AMETHYST);

        if (pattern.isPresent() && material.isPresent()) {
            ArmorTrim trim = new ArmorTrim(material.get(), pattern.get());
            stack.set(DataComponents.TRIM, trim);
        }
    }
}