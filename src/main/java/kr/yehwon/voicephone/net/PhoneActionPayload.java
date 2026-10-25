package kr.yehwon.voicephone.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PhoneActionPayload(String action, String target) implements CustomPacketPayload {
    public static final Type<PhoneActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("voicephone", "action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhoneActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PhoneActionPayload::action,
            ByteBufCodecs.STRING_UTF8, PhoneActionPayload::target,
            PhoneActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
