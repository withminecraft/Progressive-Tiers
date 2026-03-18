package net.Momo_EMT.enhanced_monster.client;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class MyJadePlugin implements IWailaPlugin {


    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(EMJadeProvider.INSTANCE, LivingEntity.class);
    }
}