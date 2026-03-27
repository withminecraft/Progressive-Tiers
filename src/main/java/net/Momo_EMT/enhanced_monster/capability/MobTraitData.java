package net.Momo_EMT.enhanced_monster.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import java.util.HashMap;
import java.util.Map;

public class MobTraitData implements INBTSerializable<CompoundTag> {
    private final Map<String, Integer> traits = new HashMap<>();
    private int quality = 0;
    private boolean processed = false;
    private boolean isBoss = false;

    public MobTraitData() {}

    public void addTrait(String tag, int level) { this.traits.put(tag, level); }
    public Map<String, Integer> getTraits() { return this.traits; }
    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    public boolean isBoss() { return isBoss; }
    public void setBoss(boolean isBoss) { this.isBoss = isBoss; }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return serializeNBT(); 
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        deserializeNBT(nbt); 
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Quality", this.quality);
        nbt.putBoolean("Processed", this.processed);
        nbt.putBoolean("IsBoss", this.isBoss);
        CompoundTag traitsTag = new CompoundTag();
        this.traits.forEach(traitsTag::putInt);
        nbt.put("Traits", traitsTag);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.quality = nbt.getInt("Quality");
        this.processed = nbt.getBoolean("Processed");
        this.isBoss = nbt.getBoolean("IsBoss");
        this.traits.clear();
        if (nbt.contains("Traits", Tag.TAG_COMPOUND)) {
            CompoundTag traitsTag = nbt.getCompound("Traits");
            for (String key : traitsTag.getAllKeys()) {
                this.traits.put(key, traitsTag.getInt(key));
            }
        }
    }
}