package net.Momo_EMT.enhanced_monster.network;

import net.Momo_EMT.enhanced_monster.EffectAllocator;
import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.Momo_EMT.enhanced_monster.capability.MobTraitAttachment;
import net.Momo_EMT.enhanced_monster.capability.MobTraitData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncMobTrait(int entityId, CompoundTag tag) implements CustomPacketPayload {

    public static final Type<PacketSyncMobTrait> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(EnhancedMonster.MODID, "sync_mob_trait")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncMobTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketSyncMobTrait::entityId,
            ByteBufCodecs.COMPOUND_TAG, PacketSyncMobTrait::tag,
            PacketSyncMobTrait::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketSyncMobTrait payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(payload.entityId());
                if (entity != null) {
                    CompoundTag nbt = entity.getPersistentData();
                    nbt.put("EM_SyncData", payload.tag());
                    nbt.putInt(EffectAllocator.TAG_QUALITY, payload.tag().getInt("Quality"));
                    nbt.putBoolean("IsBoss", payload.tag().getBoolean("IsBoss"));

                    MobTraitData data = entity.getData(MobTraitAttachment.MOB_TRAIT);
                    data.deserializeNBT(payload.tag());
                }
            }
        });
    }
}