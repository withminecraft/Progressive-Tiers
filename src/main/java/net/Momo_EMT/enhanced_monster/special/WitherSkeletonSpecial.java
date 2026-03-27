package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.fml.ModList;

public class WitherSkeletonSpecial implements ISpecialElite {
    public static final String TAG_DROP_SKULL = "em_drop_wither_skull";
    public static final String TAG_DROP_IGNITIUM = "em_drop_ignitium_ingot";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof WitherSkeleton wither)) return;

        var enchantments = wither.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        if (isIgnisDefeated(wither)) {
            applyCataclysmTier(wither, enchantments);
        } else {
            applyNetheriteTier(wither, enchantments);
        }

        wither.getPersistentData().putBoolean(TAG_DROP_SKULL, true);
    }

    private void applyNetheriteTier(WitherSkeleton wither, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchants) {
        wither.setItemSlot(EquipmentSlot.HEAD, createEliteArmor(wither, Items.NETHERITE_HELMET, enchants));
        wither.setItemSlot(EquipmentSlot.CHEST, createEliteArmor(wither, Items.NETHERITE_CHESTPLATE, enchants));
        wither.setItemSlot(EquipmentSlot.LEGS, createEliteArmor(wither, Items.NETHERITE_LEGGINGS, enchants));
        wither.setItemSlot(EquipmentSlot.FEET, createEliteArmor(wither, Items.NETHERITE_BOOTS, enchants));
        
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        applyEliteWeaponMods(sword, enchants);
        wither.setItemSlot(EquipmentSlot.MAINHAND, sword);
    }

    private void applyCataclysmTier(WitherSkeleton wither, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchants) {
        wither.setItemSlot(EquipmentSlot.HEAD, createEliteModArmor("cataclysm:ignitium_helmet", enchants));
        wither.setItemSlot(EquipmentSlot.CHEST, createEliteModArmor("cataclysm:ignitium_chestplate", enchants));
        wither.setItemSlot(EquipmentSlot.LEGS, createEliteModArmor("cataclysm:ignitium_leggings", enchants));
        wither.setItemSlot(EquipmentSlot.FEET, createEliteModArmor("cataclysm:ignitium_boots", enchants));
        
        ItemStack weapon = getModItem("cataclysm:the_incinerator");
        applyEliteWeaponMods(weapon, enchants);
        wither.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        
        wither.getPersistentData().putBoolean(TAG_DROP_IGNITIUM, true);
    }

    private ItemStack createEliteArmor(LivingEntity entity, Item item, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchants) {
        ItemStack stack = new ItemStack(item);
        applyEliteStatus(stack, enchants);
        stack.enchant(enchants.getOrThrow(Enchantments.PROTECTION), 1);

        if (item instanceof ArmorItem) {
            var registryAccess = entity.level().registryAccess();
            var patterns = registryAccess.lookupOrThrow(Registries.TRIM_PATTERN);
            var materials = registryAccess.lookupOrThrow(Registries.TRIM_MATERIAL);

            ArmorTrim trim = new ArmorTrim(materials.getOrThrow(TrimMaterials.DIAMOND), patterns.getOrThrow(TrimPatterns.RIB));
            stack.set(DataComponents.TRIM, trim);
        }
        return stack;
    }

    private ItemStack createEliteModArmor(String registryName, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchants) {
        ItemStack stack = getModItem(registryName);
        applyEliteStatus(stack, enchants);
        stack.enchant(enchants.getOrThrow(Enchantments.PROTECTION), 1);
        return stack;
    }

    private void applyEliteWeaponMods(ItemStack stack, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchants) {
        applyEliteStatus(stack, enchants);
        stack.enchant(enchants.getOrThrow(Enchantments.SHARPNESS), 3);
    }

    private void applyEliteStatus(ItemStack stack, net.minecraft.core.HolderLookup.RegistryLookup<Enchantment> enchants) {
        if (stack.isEmpty()) return;
        stack.enchant(enchants.getOrThrow(Enchantments.VANISHING_CURSE), 1);
        stack.enchant(enchants.getOrThrow(Enchantments.BINDING_CURSE), 1);
        
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    private boolean isIgnisDefeated(LivingEntity entity) {
        if (!ModList.get().isLoaded("cataclysm") || entity.getServer() == null) return false;

        ResourceLocation advId = ResourceLocation.fromNamespaceAndPath("cataclysm", "kill_ignis");
        AdvancementHolder adv = entity.getServer().getAdvancements().get(advId);
        if (adv == null) return false;

        for (ServerPlayer player : entity.getServer().getPlayerList().getPlayers()) {
            if (player.getAdvancements().getOrStartProgress(adv).isDone()) return true;
        }
        return false;
    }

    private ItemStack getModItem(String registryName) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(registryName));
        if (item == Items.AIR && !registryName.equals("minecraft:air")) {
            return new ItemStack(Items.NETHERITE_SWORD);
        }
        return new ItemStack(item);
    }
}