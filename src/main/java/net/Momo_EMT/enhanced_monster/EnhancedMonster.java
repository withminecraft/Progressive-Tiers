package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.client.ClothConfigScreen;
import net.Momo_EMT.enhanced_monster.capability.MobTraitAttachment;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait;
import net.Momo_EMT.enhanced_monster.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(EnhancedMonster.MODID)
public class EnhancedMonster {
    public static final String MODID = "enhanced_monster";

    public EnhancedMonster(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(Type.COMMON, net.Momo_EMT.enhanced_monster.ModConfig.SPEC, "enhanced_monster-common.toml");

        ModItems.ITEMS.register(modEventBus);

        MobTraitAttachment.ATTACHMENT_TYPES.register(modEventBus);

        modEventBus.addListener(this::registerNetworking);

        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);

        if (FMLEnvironment.dist.isClient()) {
           modContainer.registerExtensionPoint(IConfigScreenFactory.class, 
                (client, parent) -> ClothConfigScreen.create(parent));
        }
    }

    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                PacketSyncMobTrait.TYPE,
                PacketSyncMobTrait.STREAM_CODEC,
                PacketSyncMobTrait::handle
        );
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == net.Momo_EMT.enhanced_monster.ModConfig.SPEC) {
            net.Momo_EMT.enhanced_monster.ModConfig.bakeConfig();
        }
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == net.Momo_EMT.enhanced_monster.ModConfig.SPEC) {
            net.Momo_EMT.enhanced_monster.ModConfig.bakeConfig();
        }
    }
}
