package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class HuskSpecial implements ISpecialElite {
    public static final String TAG_DROP_ANCIENT_LOOT = "em_drop_ancient_loot";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Husk husk)) return;

        if (isAncientRemnantDefeated(husk)) {
            var koboletonType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse("cataclysm:koboleton"));
            if (koboletonType != null) {
                Entity mount = koboletonType.create(husk.level());
                
                if (mount instanceof LivingEntity koboleton) {
                    mount.moveTo(husk.getX(), husk.getY(), husk.getZ(), husk.getYRot(), husk.getXRot());

                    koboleton.getPersistentData().putBoolean("EM_SkipAllocation", true);
                    
                    husk.level().addFreshEntity(mount);
                    
                    husk.startRiding(mount);
                }
            }
            
            ItemStack helmet = prepareEliteItem("cataclysm:bone_reptile_helmet");
            helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
            husk.setItemSlot(EquipmentSlot.HEAD, helmet);

            ItemStack chestplate = prepareEliteItem("cataclysm:bone_reptile_chestplate");
            chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
            husk.setItemSlot(EquipmentSlot.CHEST, chestplate);

            ItemStack spear = prepareEliteItem("cataclysm:ancient_spear");
            spear.enchant(Enchantments.SHARPNESS, 3); 
            spear.enchant(Enchantments.KNOCKBACK, 2);
            spear.enchant(Enchantments.FIRE_ASPECT, 2);
            husk.setItemSlot(EquipmentSlot.MAINHAND, spear);

            husk.getPersistentData().putBoolean(TAG_DROP_ANCIENT_LOOT, true);
        }
    }

    private ItemStack prepareEliteItem(String registryName) {
        ItemStack stack = getModItem(registryName);
        if (!stack.isEmpty()) {
            stack.enchant(Enchantments.VANISHING_CURSE, 1);
            stack.enchant(Enchantments.BINDING_CURSE, 1);
            stack.getOrCreateTag().putBoolean("Unbreakable", true);
        }
        return stack;
    }

    private boolean isAncientRemnantDefeated(LivingEntity entity) {
        if (!ModList.get().isLoaded("cataclysm") || entity.getServer() == null) return false;

        ResourceLocation advId = ResourceLocation.tryParse("cataclysm:kill_remnant");
        Advancement adv = entity.getServer().getAdvancements().getAdvancement(advId);
        if (adv == null) return false;

        for (ServerPlayer player : entity.getServer().getPlayerList().getPlayers()) {
            if (player.getAdvancements().getOrStartProgress(adv).isDone()) return true;
        }
        return false;
    }

    private ItemStack getModItem(String registryName) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(registryName));
        return new ItemStack(item != null ? item : Items.AIR);
    }
}