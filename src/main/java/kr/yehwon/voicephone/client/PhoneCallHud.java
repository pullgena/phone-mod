package kr.yehwon.voicephone.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;

/** In-game HUD, rather than keeping the Voice Chat group window on-screen. */
public final class PhoneCallHud {
    private PhoneCallHud(){}
    public static void register() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("voicephone", "call_indicator"), (graphics, delta) ->
                VoicePhoneClient.extractHud(graphics, Minecraft.getInstance()));
    }
}
