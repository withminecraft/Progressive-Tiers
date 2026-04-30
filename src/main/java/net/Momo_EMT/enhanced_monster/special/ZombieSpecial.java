package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class ZombieSpecial implements ISpecialElite {
    public static final String TAG_REDIRECT = "em_redirect_damage";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Zombie zombie)) return;

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

            horse.getPersistentData().putLong("em_special_spawn_tick", zombie.level().getGameTime());

            zombie.level().addFreshEntity(horse);
            zombie.startRiding(horse);
        }

        equip(zombie, EquipmentSlot.HEAD, Items.DIAMOND_HELMET, 0);
        equip(zombie, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE, 0);
        equip(zombie, EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS, 0);
        equip(zombie, EquipmentSlot.FEET, Items.DIAMOND_BOOTS, 0);

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 3);
        sword.enchant(Enchantments.KNOCKBACK, 2);
        sword.enchant(Enchantments.FIRE_ASPECT, 2);
        sword.enchant(Enchantments.VANISHING_CURSE, 1);
        sword.enchant(Enchantments.BINDING_CURSE, 1);
        sword.getOrCreateTag().putBoolean("Unbreakable", true);
        
        zombie.setItemSlot(EquipmentSlot.MAINHAND, sword);
    }

    private void equip(Zombie zombie, EquipmentSlot slot, Item item, int dummy) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        stack.enchant(Enchantments.VANISHING_CURSE, 1);
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        
        applyWildAmethystTrim(stack);
        
        zombie.setItemSlot(slot, stack);
    }

    private void applyWildAmethystTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag trimTag = new CompoundTag();

        trimTag.putString("pattern", "minecraft:wild");
        trimTag.putString("material", "minecraft:amethyst");

        nbt.put("Trim", trimTag);
    }
}