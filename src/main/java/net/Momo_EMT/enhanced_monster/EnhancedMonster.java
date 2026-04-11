package net.Momo_EMT.enhanced_monster;

import net.Momo_EMT.enhanced_monster.client.ClothConfigScreen;
import net.Momo_EMT.enhanced_monster.capability.IMobTrait;
import net.Momo_EMT.enhanced_monster.network.PacketSyncMobTrait; 
import net.Momo_EMT.enhanced_monster.item.ModItems;
import net.Momo_EMT.enhanced_monster.item.ModItemEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(EnhancedMonster.MODID)
public class EnhancedMonster {
    public static final String MODID = "enhanced_monster";

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public EnhancedMonster(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.ITEMS.register(modEventBus);

        context.registerConfig(Type.COMMON, net.Momo_EMT.enhanced_monster.ModConfig.SPEC, "enhanced_monster-common.toml");
        
        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);
        
        modEventBus.addListener(this::registerCaps);

        if (net.minecraftforge.fml.loading.FMLEnvironment.dist.isClient()) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, 
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> ClothConfigScreen.create(parent)));
        }
        
        int id = 0;
        CHANNEL.registerMessage(id++, 
            PacketSyncMobTrait.class, 
            PacketSyncMobTrait::encode, 
            PacketSyncMobTrait::decode, 
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

    private void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(IMobTrait.class);
    }
}
