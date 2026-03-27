package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class WitherSkeletonSpecial implements ISpecialElite {
    public static final String TAG_DROP_SKULL = "em_drop_wither_skull";
    public static final String TAG_DROP_IGNITIUM = "em_drop_ignitium_ingot";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof WitherSkeleton wither)) return;

        if (isIgnisDefeated(wither)) {
            applyCataclysmTier(wither);
        } else {
            applyNetheriteTier(wither);
        }

        wither.getPersistentData().putBoolean(TAG_DROP_SKULL, true);
    }

    private void applyNetheriteTier(WitherSkeleton wither) {
        wither.setItemSlot(EquipmentSlot.HEAD, createEliteArmor(Items.NETHERITE_HELMET));
        wither.setItemSlot(EquipmentSlot.CHEST, createEliteArmor(Items.NETHERITE_CHESTPLATE));
        wither.setItemSlot(EquipmentSlot.LEGS, createEliteArmor(Items.NETHERITE_LEGGINGS));
        wither.setItemSlot(EquipmentSlot.FEET, createEliteArmor(Items.NETHERITE_BOOTS));
        
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        applyEliteWeaponMods(sword);
        wither.setItemSlot(EquipmentSlot.MAINHAND, sword);
    }

    private void applyCataclysmTier(WitherSkeleton wither) {
        wither.setItemSlot(EquipmentSlot.HEAD, createEliteModArmor("cataclysm:ignitium_helmet"));
        wither.setItemSlot(EquipmentSlot.CHEST, createEliteModArmor("cataclysm:ignitium_chestplate"));
        wither.setItemSlot(EquipmentSlot.LEGS, createEliteModArmor("cataclysm:ignitium_leggings"));
        wither.setItemSlot(EquipmentSlot.FEET, createEliteModArmor("cataclysm:ignitium_boots"));
        
        ItemStack weapon = getModItem("cataclysm:the_incinerator");
        applyEliteWeaponMods(weapon);
        wither.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        
        wither.getPersistentData().putBoolean(TAG_DROP_IGNITIUM, true);
    }

    private ItemStack createEliteArmor(Item item) {
        ItemStack stack = new ItemStack(item);
        applyEliteStatus(stack);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        if (item instanceof ArmorItem) {
            CompoundTag nbt = stack.getOrCreateTag();
            CompoundTag trimTag = new CompoundTag();
            trimTag.putString("pattern", "minecraft:rib");
            trimTag.putString("material", "minecraft:diamond");
            nbt.put("Trim", trimTag);
        }
        return stack;
    }

    private ItemStack createEliteModArmor(String registryName) {
        ItemStack stack = getModItem(registryName);
        applyEliteStatus(stack);
        stack.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        return stack;
    }

    private void applyEliteWeaponMods(ItemStack stack) {
        applyEliteStatus(stack);
        stack.enchant(Enchantments.SHARPNESS, 3);
    }

    private void applyEliteStatus(ItemStack stack) {
        if (stack.isEmpty()) return;
        stack.enchant(Enchantments.VANISHING_CURSE, 1);
        stack.enchant(Enchantments.BINDING_CURSE, 1);
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
    }

    private boolean isIgnisDefeated(LivingEntity entity) {
        if (!ModList.get().isLoaded("cataclysm") || entity.getServer() == null) return false;

        ResourceLocation advId = ResourceLocation.tryParse("cataclysm:kill_ignis");
        Advancement adv = entity.getServer().getAdvancements().getAdvancement(advId);
        if (adv == null) return false;

        for (ServerPlayer player : entity.getServer().getPlayerList().getPlayers()) {
            if (player.getAdvancements().getOrStartProgress(adv).isDone()) return true;
        }
        return false;
    }

    private ItemStack getModItem(String registryName) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(registryName));
        return new ItemStack(item != null ? item : Items.NETHERITE_SWORD);
    }
}