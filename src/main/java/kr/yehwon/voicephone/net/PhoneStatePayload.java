package kr.yehwon.voicephone.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PhoneStatePayload(String state, String peer, String detail, boolean open)
        implements CustomPacketPayload {
    public static final Type<PhoneStatePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("voicephone", "state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PhoneStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PhoneStatePayload::state,
            ByteBufCodecs.STRING_UTF8, PhoneStatePayload::peer,
            ByteBufCodecs.STRING_UTF8, PhoneStatePayload::detail,
            ByteBufCodecs.BOOL, PhoneStatePayload::open,
            PhoneStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
