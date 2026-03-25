package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class PiglinSpecial implements ISpecialElite {
    public static final String TAG_DROP_GOLD = "em_drop_piglin_gold";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Piglin piglin)) return;

        piglin.setCanPickUpLoot(false);

        equipGoldArmor(piglin, EquipmentSlot.HEAD, Items.GOLDEN_HELMET);
        equipGoldArmor(piglin, EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE);
        equipGoldArmor(piglin, EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS);
        equipGoldArmor(piglin, EquipmentSlot.FEET, Items.GOLDEN_BOOTS);

        if (piglin.level().random.nextFloat() < 0.5F) {
            ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
            sword.enchant(Enchantments.SHARPNESS, 5);
            sword.enchant(Enchantments.FIRE_ASPECT, 2);
            piglin.setItemSlot(EquipmentSlot.MAINHAND, sword);
        } else {
            ItemStack crossbow = new ItemStack(Items.CROSSBOW);
            crossbow.enchant(Enchantments.QUICK_CHARGE, 3);
            crossbow.enchant(Enchantments.MULTISHOT, 1);
            piglin.setItemSlot(EquipmentSlot.MAINHAND, crossbow);
        }

        piglin.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        piglin.getPersistentData().putBoolean(TAG_DROP_GOLD, true);
    }

    private void equipGoldArmor(Piglin piglin, EquipmentSlot slot, net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        
        applySnoutNetheriteTrim(stack);
        
        piglin.setItemSlot(slot, stack);
        piglin.setDropChance(slot, 0.0F);
    }

    private void applySnoutNetheriteTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag trimTag = new CompoundTag();

        trimTag.putString("pattern", "minecraft:snout");
        trimTag.putString("material", "minecraft:netherite");

        nbt.put("Trim", trimTag);
    }
}