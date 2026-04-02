package net.Momo_EMT.enhanced_monster.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;

public class StructureValidator {

    private static final Map<String, Set<String>> STRUCTURE_MOB_MAP = Map.of(
        "fortress", Set.of("minecraft:wither_skeleton"),
        "bastion_remnant", Set.of("minecraft:piglin", "minecraft:piglin_brute"),
        "mansion", Set.of("minecraft:vindicator", "minecraft:evoker"),
        "stronghold", Set.of("minecraft:creeper", "minecraft:spider")
    );

    public static boolean isEntityInSpecialStructure(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return false;

        ResourceLocation entityRL = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityRL == null) return false;
        String entityId = entityRL.toString();

        for (Map.Entry<String, Set<String>> entry : STRUCTURE_MOB_MAP.entrySet()) {
            if (entry.getValue().contains(entityId)) {
                if (isInsideFlexibleStructure(serverLevel, entity, entry.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInsideFlexibleStructure(ServerLevel level, LivingEntity entity, String keyword) {
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        
        for (var entry : registry.entrySet()) {
            String currentStructureId = entry.getKey().location().toString();
            
            if (currentStructureId.contains(keyword)) {
                if (level.structureManager().getStructureAt(entity.blockPosition(), entry.getValue()).isValid()) {
                    return true;
                }
            }
        }
        return false;
    }
}