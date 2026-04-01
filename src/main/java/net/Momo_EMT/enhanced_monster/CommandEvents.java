package net.Momo_EMT.enhanced_monster;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = "enhanced_monster", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("emon")
            .requires(source -> source.hasPermission(2)) // 权限等级 2 (OP)
            .then(Commands.literal("reload")
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("§6[Enhanced Monster] §f正在尝试重载配置..."), true);
                    
                    try {
                        ConfigTracker.INSTANCE.loadDefaultServerConfigs();
                        
                        context.getSource().sendSuccess(() -> 
                            Component.literal("§a[Enhanced Monster] 配置已完成重载！"), true);
                        context.getSource().sendSuccess(() -> 
                            Component.literal("§7(提示：变更仅对重载后新生成的怪物生效)"), false);
                            
                    } catch (Exception e) {
                        context.getSource().sendFailure(Component.literal("§c[Enhanced Monster] 重载过程中发生错误，请检查控制台！"));
                        e.printStackTrace();
                    }
                    
                    return 1;
                })
            ));
    }
}