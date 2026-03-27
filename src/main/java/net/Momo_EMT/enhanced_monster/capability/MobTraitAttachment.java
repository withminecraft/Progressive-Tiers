package net.Momo_EMT.enhanced_monster.capability;

import net.Momo_EMT.enhanced_monster.EnhancedMonster;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag; 
import java.util.function.Supplier;

public class MobTraitAttachment {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EnhancedMonster.MODID);

        public static final Supplier<AttachmentType<MobTraitData>> MOB_TRAIT = ATTACHMENT_TYPES.register(
                "mob_trait",
                () -> AttachmentType.builder(MobTraitData::new)
                        .serialize(new IAttachmentSerializer<CompoundTag, MobTraitData>() {
                        @Override
                        public MobTraitData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                                MobTraitData data = new MobTraitData();
                                data.deserializeNBT(provider, tag);
                                return data;
                        }

                        @Override
                        public CompoundTag write(MobTraitData data, HolderLookup.Provider provider) {
                                return data.serializeNBT(provider);
                        }
                        })
                        .copyOnDeath()
                        .build()
        );
}