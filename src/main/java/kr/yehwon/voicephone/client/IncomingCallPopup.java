package kr.yehwon.voicephone.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Small incoming call popup when the phone is in the inventory but not held. */
public final class IncomingCallPopup extends Screen {
    public IncomingCallPopup() {
        super(Component.literal("Voice Phone - Incoming call"));
    }

    private int panelX() { return Math.max(8, width - 232); }
    private int panelY() { return 22; }

    @Override
    protected void init() {
        int x = panelX(), y = panelY();
        if ("incoming".equals(VoicePhoneClient.state)) {
            addRenderableWidget(Button.builder(Component.literal("받기"), b -> PhoneScreen.send("accept", ""))
                    .bounds(x + 12, y + 64, 96, 22).build());
            addRenderableWidget(Button.builder(Component.literal("거절"), b -> PhoneScreen.send("reject", ""))
                    .bounds(x + 116, y + 64, 96, 22).build());
        } else if ("active".equals(VoicePhoneClient.state)) {
            addRenderableWidget(Button.builder(Component.literal("통화 종료"), b -> PhoneScreen.send("hangup", ""))
                    .bounds(x + 12, y + 64, 200, 22).build());
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int x = panelX(), y = panelY();
        g.fill(x, y, x + 224, y + 99, 0xF0182638);
        g.outline(x, y, 224, 99, 0xFF669CD4);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = panelX(), y = panelY();
        boolean active = "active".equals(VoicePhoneClient.state);
        g.text(font, active ? "VOICE PHONE · 통화 중" : "VOICE PHONE · 전화 수신",
                x + 12, y + 11, 0xFF9FD3FF, true);
        g.text(font, VoicePhoneClient.peer, x + 12, y + 30, 0xFFFFFFFF, true);
        g.text(font, active ? "휴대폰을 들지 않아도 통화할 수 있습니다" : "전화가 왔습니다",
                x + 12, y + 46, 0xFFDCE7F3, false);
    }
}
