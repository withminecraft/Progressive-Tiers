package net.Momo_EMT.enhanced_monster.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public class MobTraitStorage implements INBTSerializable<CompoundTag> {
    private final IMobTrait instance;

    public MobTraitStorage(IMobTrait instance) {
        this.instance = instance;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Quality", instance.getQuality());
        tag.putBoolean("Processed", instance.isProcessed());
        tag.putBoolean("IsBoss", instance.isBoss());

        CompoundTag traitsTag = new CompoundTag();
        instance.getTraits().forEach(traitsTag::putInt);
        tag.put("Traits", traitsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.setQuality(nbt.getInt("Quality"));
        instance.setProcessed(nbt.getBoolean("Processed"));
        instance.setBoss(nbt.getBoolean("IsBoss"));

        if (nbt.contains("Traits", Tag.TAG_COMPOUND)) {
            CompoundTag traitsTag = nbt.getCompound("Traits");
            for (String key : traitsTag.getAllKeys()) {
                instance.addTrait(key, traitsTag.getInt(key));
            }
        }
    }
}