package kr.yehwon.voicephone.client;

import kr.yehwon.voicephone.VoicePhone;
import kr.yehwon.voicephone.net.PhoneStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

/** Client receiver screen selection and main-menu update entry point. */
public final class VoicePhoneClient implements ClientModInitializer {
    static String state = "idle";
    static String peer = "";
    static String detail = "";

    private static boolean holdingPhone(Minecraft mc) {
        return mc.player != null
                && (mc.player.getMainHandItem().is(VoicePhone.PHONE) || mc.player.getOffhandItem().is(VoicePhone.PHONE));
    }

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen) {
                Screens.getWidgets(screen).add(Button.builder(Component.literal("Voice Phone 업데이트"),
                        b -> client.gui.setScreen(new PhoneUpdateScreen(screen)))
                        .bounds(Math.max(5, w - 178), 12, 170, 20).build());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(PhoneStatePayload.TYPE, (payload, context) -> {
            state = payload.state();
            peer = payload.peer();
            detail = payload.detail();
            Minecraft mc = context.client();
            boolean held = holdingPhone(mc);
            boolean call = "incoming".equals(state) || "active".equals(state);
            boolean visible = mc.gui.screen() instanceof PhoneScreen || mc.gui.screen() instanceof IncomingCallPopup;

            if (call && !held) {
                // Receiving and continuing a call do not require holding the item.
                mc.gui.setScreen(new IncomingCallPopup());
            } else if (payload.open() || visible) {
                if ("idle".equals(state) && mc.gui.screen() instanceof IncomingCallPopup) {
                    mc.gui.setScreen(null);
                } else {
                    mc.gui.setScreen(new PhoneScreen());
                }
            }
        });
    }
}
