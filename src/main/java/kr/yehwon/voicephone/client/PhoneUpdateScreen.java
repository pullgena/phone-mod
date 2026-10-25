package kr.yehwon.voicephone.client;

import kr.yehwon.voicephone.update.PhoneUpdater;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Usable in the title screen before opening any singleplayer or multiplayer world. */
public final class PhoneUpdateScreen extends Screen {
    private final Screen previous;
    private String feedback = "";

    public PhoneUpdateScreen(Screen previous) {
        super(Component.literal("Voice Phone Update"));
        this.previous = previous;
    }

    @Override
    protected void init() {
        int x = width / 2, y = height / 2;
        addRenderableWidget(Button.builder(Component.literal("업데이트 확인 및 다운로드"), b -> {
            boolean started = PhoneUpdater.checkNow();
            feedback = started ? "GitHub Releases를 확인하고 있습니다..." : PhoneUpdater.status();
        }).bounds(x - 116, y + 32, 232, 24).build());
        addRenderableWidget(Button.builder(Component.literal("메인 화면으로"), b -> minecraft.gui.setScreen(previous))
                .bounds(x - 116, y + 64, 232, 22).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fillGradient(0, 0, width, height, 0xFF102138, 0xFF080F19);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = width / 2, y = height / 2;
        g.centeredText(font, "VOICE PHONE · 업데이트", x, y - 74, 0xFF99CAFF);
        g.centeredText(font, PhoneUpdater.status(), x, y - 35, 0xFFFFFFFF);
        if (!feedback.isBlank()) g.centeredText(font, feedback, x, y - 14, 0xFFB6CBE6);
        g.centeredText(font, "업데이트 후 게임을 종료하고 다시 실행해 주세요.", x, y + 109, 0xFFADBDCC);
    }
}
