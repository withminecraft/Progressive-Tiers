package net.Momo_EMT.enhanced_monster.capability;

import net.minecraft.nbt.CompoundTag;
import java.util.HashMap;
import java.util.Map;

public class MobTrait implements IMobTrait {
    private final Map<String, Integer> traits = new HashMap<>();
    private int quality = 0;
    private boolean processed = false;
    private boolean isBoss = false;

    @Override
    public void addTrait(String tag, int level) {
        this.traits.put(tag, level);
    }

    @Override
    public Map<String, Integer> getTraits() {
        return this.traits;
    }

    @Override public int getQuality() { return quality; }
    @Override public void setQuality(int quality) { this.quality = quality; }

    @Override public boolean isProcessed() { return processed; }
    @Override public void setProcessed(boolean processed) { this.processed = processed; }

    @Override public boolean isBoss() { return isBoss; }
    @Override public void setBoss(boolean isBoss) { this.isBoss = isBoss; }


    @Override
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

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.quality = nbt.getInt("Quality");
        this.processed = nbt.getBoolean("Processed");
        this.isBoss = nbt.getBoolean("IsBoss");
        
        this.traits.clear();
        if (nbt.contains("Traits")) {
            CompoundTag traitsTag = nbt.getCompound("Traits");
            for (String key : traitsTag.getAllKeys()) {
                this.traits.put(key, traitsTag.getInt(key));
            }
        }
    }
}