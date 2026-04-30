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

    private double initialMaxHealth = -1;
    private int regenCooldownTicks = 0;
    private int regenActiveTicks = 0;

    private long voidCooldown = 0;

    private int healInhibitTicks = 0;

    public MobTraitData() {}

    public void addTrait(String tag, int level) { this.traits.put(tag, level); }
    public Map<String, Integer> getTraits() { return this.traits; }
    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    public boolean isBoss() { return isBoss; }
    public void setBoss(boolean isBoss) { this.isBoss = isBoss; }

    public double getInitialMaxHealth() { return initialMaxHealth; }
    public void setInitialMaxHealth(double initialMaxHealth) { this.initialMaxHealth = initialMaxHealth; }
    public int getRegenCooldownTicks() { return regenCooldownTicks; }
    public void setRegenCooldownTicks(int ticks) { this.regenCooldownTicks = ticks; }
    public int getRegenActiveTicks() { return regenActiveTicks; }
    public void setRegenActiveTicks(int ticks) { this.regenActiveTicks = ticks; }

    public long getVoidCooldown() { return voidCooldown; }
    public void setVoidCooldown(long voidCooldown) { this.voidCooldown = voidCooldown; }

    public int getHealInhibitTicks() { return healInhibitTicks; }
    public void setHealInhibitTicks(int ticks) { this.healInhibitTicks = ticks; }

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

        nbt.putDouble("InitialMaxHealth", this.initialMaxHealth);
        nbt.putInt("RegenCDTicks", this.regenCooldownTicks);
        nbt.putInt("RegenActiveTicks", this.regenActiveTicks);

        nbt.putLong("VoidCD", this.voidCooldown);

        nbt.putInt("HealInhibitTicks", this.healInhibitTicks);

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.quality = nbt.getInt("Quality");
        this.processed = nbt.getBoolean("Processed");
        this.isBoss = nbt.getBoolean("IsBoss");

        this.initialMaxHealth = nbt.getDouble("InitialMaxHealth");
        this.regenCooldownTicks = nbt.getInt("RegenCDTicks");
        this.regenActiveTicks = nbt.getInt("RegenActiveTicks");

        this.voidCooldown = nbt.getLong("VoidCD");

        this.healInhibitTicks = nbt.getInt("HealInhibitTicks");

        this.traits.clear();
        if (nbt.contains("Traits", Tag.TAG_COMPOUND)) {
            CompoundTag traitsTag = nbt.getCompound("Traits");
            for (String key : traitsTag.getAllKeys()) {
                this.traits.put(key, traitsTag.getInt(key));
            }
        }
    }
}