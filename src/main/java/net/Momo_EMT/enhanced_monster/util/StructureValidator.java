package net.Momo_EMT.enhanced_monster.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

public class StructureValidator {

    private static final Map<String, Set<String>> STRUCTURE_MOB_MAP = Map.ofEntries(
        entry("fortress", Set.of("minecraft:wither_skeleton")),
        entry("bastion_remnant", Set.of("minecraft:piglin", "minecraft:piglin_brute")),
        entry("mansion", Set.of("minecraft:vindicator", "minecraft:evoker")),
        entry("stronghold", Set.of("minecraft:creeper", "minecraft:spider")),
        entry("citadel", Set.of("irons_spellbooks:citadel_keeper")),
        entry("shelter", Set.of("minecraft:vindicator", "minecraft:evoker")),
        entry("wind_shrine", Set.of("minecraft:ravager", "goety:crusher", "goety:storm_caster")),
        entry("ominous_blacksmith", Set.of("minecraft:ravager", "goety:crusher", "goety:storm_caster")),
        entry("dark_manor", Set.of("minecraft:vindicator", "minecraft:witch", "minecraft:illusioner", "goety:piker")),
        entry("sorcerous_keep", Set.of("goety:sorcerer")),
        entry("crypt", Set.of("goety:cairn_necromancer"))
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
        BlockPos pos = entity.blockPosition();
        var structuresAt = level.structureManager().getAllStructuresAt(pos);

        if (structuresAt.isEmpty()) return false;

        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);

        for (Structure structure : structuresAt.keySet()) {
            if (level.structureManager().getStructureAt(pos, structure).isValid()) {
                ResourceLocation id = registry.getKey(structure);
                if (id != null && id.toString().contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }
}