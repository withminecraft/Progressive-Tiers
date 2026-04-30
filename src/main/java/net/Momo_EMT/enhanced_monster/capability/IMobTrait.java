package net.Momo_EMT.enhanced_monster.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import java.util.Map;

public interface IMobTrait extends INBTSerializable<CompoundTag> {
    
    void addTrait(String tag, int level);
    Map<String, Integer> getTraits();
    
    int getQuality();
    void setQuality(int quality);
    
    boolean isProcessed();
    void setProcessed(boolean processed);
    
    boolean isBoss();
    void setBoss(boolean isBoss);

    double getRegenInitialMaxHealth();
    void setRegenInitialMaxHealth(double health);

    int getRegenActiveTicks();
    void setRegenActiveTicks(int ticks);

    int getRegenCooldownTicks();
    void setRegenCooldownTicks(int ticks);

    long getVoidCooldown();
    void setVoidCooldown(long time);

    int getInhibitHealTicks();
    void setInhibitHealTicks(int ticks);

    @Override
    CompoundTag serializeNBT();

    @Override
    void deserializeNBT(CompoundTag nbt);
}