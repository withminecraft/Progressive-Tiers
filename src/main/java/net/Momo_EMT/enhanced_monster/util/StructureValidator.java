package net.Momo_EMT.enhanced_monster.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Map;
import java.util.Set;

public class StructureValidator {

    private static final Map<String, Set<String>> STRUCTURE_MOB_MAP = Map.of(
        "fortress", Set.of("minecraft:wither_skeleton"),
        "bastion_remnant", Set.of("minecraft:piglin", "minecraft:piglin_brute"),
        "mansion", Set.of("minecraft:vindicator", "minecraft:evoker"),
        "stronghold", Set.of("minecraft:creeper", "minecraft:spider"),
        "trial_chambers", Set.of("ALL_MOBS")
    );

    public static boolean isEntityInSpecialStructure(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return false;

        ResourceLocation entityRL = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityRL == null) return false;
        String entityId = entityRL.toString();

        for (Map.Entry<String, Set<String>> entry : STRUCTURE_MOB_MAP.entrySet()) {
            String keyword = entry.getKey();
            if (keyword.equals("trial_chambers") || entry.getValue().contains(entityId)) {
                if (isInsideFlexibleStructure(serverLevel, entity, keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInsideFlexibleStructure(ServerLevel level, LivingEntity entity, String keyword) {
        BlockPos pos = entity.blockPosition();

        var allStructuresAt = level.structureManager().getAllStructuresAt(pos);
        if (allStructuresAt.isEmpty()) return false;

        for (var entry : allStructuresAt.entrySet()) {
            Structure structure = entry.getKey();
            StructureStart start = level.structureManager().getStructureAt(pos, structure);
            
            if (start.isValid()) {
                ResourceLocation id = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(structure);
                if (id != null && id.toString().contains(keyword)) {
                    if (start.getBoundingBox().isInside(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}