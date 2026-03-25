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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class WitherSkeletonSpecial implements ISpecialElite {
    public static final String TAG_DROP_SKULL = "em_drop_wither_skull";
    public static final String TAG_DROP_IGNITIUM = "em_drop_ignitium_ingot";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof WitherSkeleton wither)) return;

        wither.setCanPickUpLoot(false);

        if (isIgnisDefeated(wither)) {
            applyCataclysmTier(wither);
        } else {
            applyNetheriteTier(wither);
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            wither.setDropChance(slot, 0.0F);
        }
        wither.getPersistentData().putBoolean(TAG_DROP_SKULL, true);
    }

    private void applyNetheriteTier(WitherSkeleton wither) {
        wither.setItemSlot(EquipmentSlot.HEAD, createTrimmedArmor(Items.NETHERITE_HELMET));
        wither.setItemSlot(EquipmentSlot.CHEST, createTrimmedArmor(Items.NETHERITE_CHESTPLATE));
        wither.setItemSlot(EquipmentSlot.LEGS, createTrimmedArmor(Items.NETHERITE_LEGGINGS));
        wither.setItemSlot(EquipmentSlot.FEET, createTrimmedArmor(Items.NETHERITE_BOOTS));
        wither.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
    }

    private void applyCataclysmTier(WitherSkeleton wither) {
        wither.setItemSlot(EquipmentSlot.HEAD, getModItem("cataclysm:ignitium_helmet"));
        wither.setItemSlot(EquipmentSlot.CHEST, getModItem("cataclysm:ignitium_chestplate"));
        wither.setItemSlot(EquipmentSlot.LEGS, getModItem("cataclysm:ignitium_leggings"));
        wither.setItemSlot(EquipmentSlot.FEET, getModItem("cataclysm:ignitium_boots"));
        wither.setItemSlot(EquipmentSlot.MAINHAND, getModItem("cataclysm:the_incinerator"));
        
        wither.getPersistentData().putBoolean(TAG_DROP_IGNITIUM, true);
    }

    private ItemStack createTrimmedArmor(Item item) {
        ItemStack stack = new ItemStack(item);
        if (item instanceof ArmorItem) {
            CompoundTag nbt = stack.getOrCreateTag();
            CompoundTag trimTag = new CompoundTag();
            trimTag.putString("pattern", "minecraft:rib");
            trimTag.putString("material", "minecraft:diamond");
            nbt.put("Trim", trimTag);
        }
        return stack;
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