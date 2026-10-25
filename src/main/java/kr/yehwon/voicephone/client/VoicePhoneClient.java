package kr.yehwon.voicephone.client;

import kr.yehwon.voicephone.VoicePhone;
import kr.yehwon.voicephone.net.PhoneStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/** State-driven incoming UI and lightweight ongoing-call HUD. */
public final class VoicePhoneClient implements ClientModInitializer {
    static String state="idle", peer="", detail="";
    private static long incomingAt=0L;
    private static long connectedAt=0L;
    private static boolean awaitingAnswer=false;

    private static boolean holdingPhone(Minecraft mc) {
        return mc.player != null && (mc.player.getMainHandItem().is(VoicePhone.PHONE)
                || mc.player.getOffhandItem().is(VoicePhone.PHONE));
    }

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h)->{
            if(screen instanceof TitleScreen) {
                Screens.getWidgets(screen).add(Button.builder(Component.literal("Voice Phone 업데이트"),
                        b -> client.gui.setScreen(new PhoneUpdateScreen(screen)))
                        .bounds(Math.max(5,w-178),12,170,20).build());
            }
        });

        PhoneCallHud.register();
        ClientPlayNetworking.registerGlobalReceiver(PhoneStatePayload.TYPE, (payload, context)->{
            Minecraft mc=context.client();
            String previous=state;
            state=payload.state(); peer=payload.peer(); detail=payload.detail();
            if("incoming".equals(state) && !"incoming".equals(previous)) {
                incomingAt=System.currentTimeMillis();
                if(mc.player!=null)mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(),0.8F,1.3F);
            }
            if("active".equals(state) && !"active".equals(previous))connectedAt=System.currentTimeMillis();

            if("active".equals(state)) {
                // Close phone screens on both ends: the call remains active in HUD only.
                if(mc.gui.screen() instanceof PhoneScreen || mc.gui.screen() instanceof IncomingCallPopup)
                    mc.gui.setScreen(null);
                return;
            }
            if("incoming".equals(state)) {
                if(holdingPhone(mc))mc.gui.setScreen(new PhoneScreen());
                else mc.gui.setScreen(new IncomingCallPopup());
                return;
            }
            if("outgoing".equals(state)) {
                if(mc.gui.screen() instanceof PhoneScreen || payload.open())mc.gui.setScreen(new PhoneScreen());
                return;
            }
            if(mc.gui.screen() instanceof IncomingCallPopup)mc.gui.setScreen(null);
            else if(mc.gui.screen() instanceof PhoneScreen && !"error".equals(state)) mc.gui.setScreen(null);
            else if("error".equals(state))mc.gui.setScreen(new PhoneScreen());
        });
    }

    public static void extractHud(GuiGraphicsExtractor g, Minecraft mc) {
        if(!"active".equals(state) || mc.player==null || mc.gui.screen()!=null)return;
        String label="통화 중  ·  "+peer;
        g.fill(8,8,Math.max(114,18+label.length()*6),31,0xD018273D);
        g.text(mc.font,label,16,16,0xFF9FE6B4,true);
    }
}
