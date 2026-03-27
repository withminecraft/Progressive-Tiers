package net.Momo_EMT.enhanced_monster.special;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.fml.ModList;

public class HuskSpecial implements ISpecialElite {
    public static final String TAG_DROP_ANCIENT_LOOT = "em_drop_ancient_loot";

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Husk husk)) return;

        if (isAncientRemnantDefeated(husk)) {
            var enchantments = husk.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            
            var koboletonType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("cataclysm:koboleton"));
            if (koboletonType != BuiltInRegistries.ENTITY_TYPE.get(BuiltInRegistries.ENTITY_TYPE.getDefaultKey())) {
                Entity mount = koboletonType.create(husk.level());
                if (mount instanceof LivingEntity koboleton) {
                    mount.moveTo(husk.getX(), husk.getY(), husk.getZ(), husk.getYRot(), husk.getXRot());
                    koboleton.getPersistentData().putBoolean("EM_SkipAllocation", true);
                    husk.level().addFreshEntity(mount);
                    husk.startRiding(mount);
                }
            }

            husk.setItemSlot(EquipmentSlot.HEAD, prepareEliteItem("cataclysm:bone_reptile_helmet", enchantments, 4));
            husk.setItemSlot(EquipmentSlot.CHEST, prepareEliteItem("cataclysm:bone_reptile_chestplate", enchantments, 4));

            ItemStack spear = prepareEliteItem("cataclysm:ancient_spear", enchantments, 0); // 0表示下面手动附魔
            if (!spear.isEmpty()) {
                spear.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 3);
                spear.enchant(enchantments.getOrThrow(Enchantments.KNOCKBACK), 2);
                spear.enchant(enchantments.getOrThrow(Enchantments.FIRE_ASPECT), 2);
            }
            husk.setItemSlot(EquipmentSlot.MAINHAND, spear);

            husk.getPersistentData().putBoolean(TAG_DROP_ANCIENT_LOOT, true);
        }
    }

    private ItemStack prepareEliteItem(String registryName, HolderLookup.RegistryLookup<Enchantment> enchantments, int protLevel) {
        ItemStack stack = getModItem(registryName);
        if (!stack.isEmpty()) {
            stack.enchant(enchantments.getOrThrow(Enchantments.VANISHING_CURSE), 1);
            stack.enchant(enchantments.getOrThrow(Enchantments.BINDING_CURSE), 1);
            
            if (protLevel > 0) {
                stack.enchant(enchantments.getOrThrow(Enchantments.PROTECTION), protLevel);
            }

            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        }
        return stack;
    }

    private boolean isAncientRemnantDefeated(LivingEntity entity) {
        if (!ModList.get().isLoaded("cataclysm") || entity.getServer() == null) return false;

        ResourceLocation advId = ResourceLocation.parse("cataclysm:kill_remnant");
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
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }
}