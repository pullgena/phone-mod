package kr.yehwon.voicephone.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

/** In-game HUD, rather than keeping the Voice Chat group window on-screen. */
public final class PhoneCallHud {
    private PhoneCallHud(){}
    public static void register() {
        HudRenderCallback.EVENT.register((graphics, delta) ->
                VoicePhoneClient.extractHud(graphics, Minecraft.getInstance()));
    }
}
