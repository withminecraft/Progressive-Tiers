package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class SkeletonSpecial implements ISpecialElite {
    public static final String TAG_REDIRECT = "em_redirect_damage";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Skeleton skeleton)) return;

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

            horse.getPersistentData().putLong("em_special_spawn_tick", skeleton.level().getGameTime());

            skeleton.level().addFreshEntity(horse);
            skeleton.startRiding(horse);
        }

        equipArmor(skeleton, EquipmentSlot.HEAD, Items.DIAMOND_HELMET);
        equipArmor(skeleton, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE);
        equipArmor(skeleton, EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS);
        equipArmor(skeleton, EquipmentSlot.FEET, Items.DIAMOND_BOOTS);

        ItemStack bow = new ItemStack(Items.BOW);
        bow.enchant(Enchantments.POWER_ARROWS, 3);
        bow.enchant(Enchantments.PUNCH_ARROWS, 2);
        bow.enchant(Enchantments.FLAMING_ARROWS, 1);
        bow.enchant(Enchantments.VANISHING_CURSE, 1);
        bow.enchant(Enchantments.BINDING_CURSE, 1);
        bow.getOrCreateTag().putBoolean("Unbreakable", true);

        skeleton.setItemSlot(EquipmentSlot.MAINHAND, bow);
    }

    private void equipArmor(Skeleton skeleton, EquipmentSlot slot, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        stack.enchant(Enchantments.VANISHING_CURSE, 1);
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        
        applyShaperRedstoneTrim(stack);
        
        skeleton.setItemSlot(slot, stack);
    }

    private void applyShaperRedstoneTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag trimTag = new CompoundTag();

        trimTag.putString("pattern", "minecraft:shaper");
        trimTag.putString("material", "minecraft:redstone");

        nbt.put("Trim", trimTag);
    }
}