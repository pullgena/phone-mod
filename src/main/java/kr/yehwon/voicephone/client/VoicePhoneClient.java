package kr.yehwon.voicephone.client;

import kr.yehwon.voicephone.net.PhoneStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class VoicePhoneClient implements ClientModInitializer {
    static String state = "idle";
    static String peer = "";
    static String detail = "";

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(PhoneStatePayload.TYPE, (payload, context) -> {
            state = payload.state();
            peer = payload.peer();
            detail = payload.detail();
            Minecraft mc = context.client();
            if (payload.open()) {
                mc.gui.setScreen(new PhoneScreen());
            }
        });
    }
}
