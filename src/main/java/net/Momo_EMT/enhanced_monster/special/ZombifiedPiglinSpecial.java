package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class ZombifiedPiglinSpecial implements ISpecialElite {
    public static final String TAG_DROP_GOLD = "em_drop_zombie_piglin_gold";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof ZombifiedPiglin zPiglin)) return;

        equipGoldArmor(zPiglin, EquipmentSlot.HEAD, Items.GOLDEN_HELMET);
        equipGoldArmor(zPiglin, EquipmentSlot.CHEST, Items.GOLDEN_CHESTPLATE);
        equipGoldArmor(zPiglin, EquipmentSlot.LEGS, Items.GOLDEN_LEGGINGS);
        equipGoldArmor(zPiglin, EquipmentSlot.FEET, Items.GOLDEN_BOOTS);

        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 7);
        sword.enchant(Enchantments.FIRE_ASPECT, 3);
        sword.enchant(Enchantments.VANISHING_CURSE, 1);
        sword.enchant(Enchantments.BINDING_CURSE, 1);
        sword.getOrCreateTag().putBoolean("Unbreakable", true);
        
        zPiglin.setItemSlot(EquipmentSlot.MAINHAND, sword);

        zPiglin.getPersistentData().putBoolean(TAG_DROP_GOLD, true);
    }

    private void equipGoldArmor(ZombifiedPiglin zPiglin, EquipmentSlot slot, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        stack.enchant(Enchantments.VANISHING_CURSE, 1);
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        
        applySnoutLapisTrim(stack);
        
        zPiglin.setItemSlot(slot, stack);
    }

    private void applySnoutLapisTrim(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem)) return;

        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag trimTag = new CompoundTag();

        trimTag.putString("pattern", "minecraft:snout");
        trimTag.putString("material", "minecraft:lapis");

        nbt.put("Trim", trimTag);
    }
}