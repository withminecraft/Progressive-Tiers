package net.Momo_EMT.enhanced_monster.capability;

import net.minecraft.nbt.CompoundTag;
import java.util.HashMap;
import java.util.Map;

public class MobTrait implements IMobTrait {
    private final Map<String, Integer> traits = new HashMap<>();
    private int quality = 0;
    private boolean processed = false;
    private boolean isBoss = false;

    private double regenInitialMaxHealth = 0;
    private int regenActiveTicks;
    private int regenCooldownTicks;

    private long voidCooldown = 0;

    private int inhibitHealTicks;

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

    @Override public double getRegenInitialMaxHealth() { return regenInitialMaxHealth; }
    @Override public void setRegenInitialMaxHealth(double health) { this.regenInitialMaxHealth = health; }

    @Override public int getRegenActiveTicks() { return regenActiveTicks; }
    @Override public void setRegenActiveTicks(int ticks) { this.regenActiveTicks = ticks; }

    @Override public int getRegenCooldownTicks() { return regenCooldownTicks; }
    @Override public void setRegenCooldownTicks(int ticks) { this.regenCooldownTicks = ticks; }

    @Override public long getVoidCooldown() { return voidCooldown; }
    @Override public void setVoidCooldown(long time) { this.voidCooldown = time; }

    @Override public int getInhibitHealTicks() { return inhibitHealTicks; }
    @Override public void setInhibitHealTicks(int ticks) { this.inhibitHealTicks = ticks; }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Quality", this.quality);
        nbt.putBoolean("Processed", this.processed);
        nbt.putBoolean("IsBoss", this.isBoss);

        nbt.putDouble("RegenInitialMaxHealth", this.regenInitialMaxHealth);
        nbt.putInt("RegenActiveTicks", this.regenActiveTicks);
        nbt.putInt("RegenCooldownTicks", this.regenCooldownTicks);

        nbt.putLong("VoidCooldown", this.voidCooldown);

        nbt.putInt("InhibitHealTicks", this.inhibitHealTicks);
        
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

        this.regenInitialMaxHealth = nbt.getDouble("RegenInitialMaxHealth");
        this.regenActiveTicks = nbt.getInt("RegenActiveTicks");
        this.regenCooldownTicks = nbt.getInt("RegenCooldownTicks");

        this.voidCooldown = nbt.getLong("VoidCooldown");

        this.inhibitHealTicks = nbt.getInt("InhibitHealTicks");
        
        this.traits.clear();
        if (nbt.contains("Traits")) {
            CompoundTag traitsTag = nbt.getCompound("Traits");
            for (String key : traitsTag.getAllKeys()) {
                this.traits.put(key, traitsTag.getInt(key));
            }
        }
    }
}