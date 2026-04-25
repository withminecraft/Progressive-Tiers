package net.Momo_EMT.enhanced_monster.network;

import net.Momo_EMT.enhanced_monster.EffectAllocator; 
import net.Momo_EMT.enhanced_monster.capability.MobTraitProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncMobTrait {
    private final int entityId;
    private final CompoundTag tag;

    public PacketSyncMobTrait(int entityId, CompoundTag tag) {
        this.entityId = entityId;
        this.tag = tag;
    }

    public static void encode(PacketSyncMobTrait msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeNbt(msg.tag);
    }

    public static PacketSyncMobTrait decode(FriendlyByteBuf buf) {
        return new PacketSyncMobTrait(buf.readInt(), buf.readNbt());
    }

    public static void handle(PacketSyncMobTrait msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
                if (entity != null) {
                    entity.getCapability(MobTraitProvider.MOB_TRAIT).ifPresent(cap -> {
                        cap.deserializeNBT(msg.tag);
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}