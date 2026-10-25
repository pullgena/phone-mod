package kr.yehwon.voicephone.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A modal call prompt: opening this screen releases the in-game mouse for clicking. */
public final class IncomingCallPopup extends Screen {
    public IncomingCallPopup() {
        super(Component.literal("Voice Phone - 전화 수신"));
    }
    private int panelX() { return Math.max(8, width - 236); }
    private int panelY() { return 22; }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean isInGameUi() { return true; }

    @Override
    protected void init() {
        int x = panelX(), y = panelY();
        addRenderableWidget(Button.builder(Component.literal("받기"), b -> PhoneScreen.send("accept", ""))
                .bounds(x + 12, y + 64, 96, 22).build());
        addRenderableWidget(Button.builder(Component.literal("거절"), b -> PhoneScreen.send("reject", ""))
                .bounds(x + 116, y + 64, 96, 22).build());
    }

    @Override
    public void onClose() {
        // Pressing ESC during ringing means declining, not silently dismissing the call.
        if ("incoming".equals(VoicePhoneClient.state)) {
            PhoneScreen.send("reject", "");
        }
        minecraft.gui.setScreen(null);
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
        g.text(font, "VOICE PHONE · 전화 수신", x + 12, y + 11, 0xFF9FD3FF, true);
        g.text(font, VoicePhoneClient.peer, x + 12, y + 30, 0xFFFFFFFF, true);
        g.text(font, "받기 / 거절 · ESC: 거절", x + 12, y + 46, 0xFFDCE7F3, false);
    }
}
