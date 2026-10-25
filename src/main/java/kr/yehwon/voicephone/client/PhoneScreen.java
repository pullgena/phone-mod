package kr.yehwon.voicephone.client;

import kr.yehwon.voicephone.net.PhoneActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Calm, translucent dialer. Visual changes only: the existing phone packet,
 * call states, actions and Simple Voice Chat integration are preserved.
 */
public final class PhoneScreen extends Screen {
    private static final long INTRO_NANOS = 260_000_000L;
    private static final int CARD_WIDTH = 236;
    private static final int CARD_HEIGHT = 190;
    private static final int MINT = 0xFF9ACFC1;
    private final long openedAt = System.nanoTime();
    private final List<AnimatedWidget> animated = new ArrayList<>();
    private EditBox playerName;

    private record AnimatedWidget(AbstractWidget widget, int baseY) {}

    public PhoneScreen() {
        super(Component.literal("Voice Phone"));
    }

    private <T extends AbstractWidget> T smoothWidget(T widget) {
        addRenderableWidget(widget);
        animated.add(new AnimatedWidget(widget, widget.getY()));
        widget.setAlpha(0.05F);
        return widget;
    }

    @Override
    protected void init() {
        animated.clear();
        playerName = null;
        int x = width / 2;
        int y = height / 2;

        switch (VoicePhoneClient.state) {
            case "incoming" -> {
                smoothWidget(Button.builder(Component.literal("받기"), b -> send("accept", ""))
                        .bounds(x - 96, y + 15, 94, 24).build());
                smoothWidget(Button.builder(Component.literal("거절"), b -> send("reject", ""))
                        .bounds(x + 2, y + 15, 94, 24).build());
            }
            case "outgoing" ->
                smoothWidget(Button.builder(Component.literal("전화 취소"), b -> send("hangup", ""))
                        .bounds(x - 96, y + 15, 192, 24).build());
            case "active" ->
                smoothWidget(Button.builder(Component.literal("통화 종료"), b -> send("hangup", ""))
                        .bounds(x - 96, y + 15, 192, 24).build());
            default -> {
                playerName = smoothWidget(new EditBox(font, x - 96, y - 15, 192, 22,
                        Component.literal("상대 플레이어")));
                playerName.setMaxLength(16);
                playerName.setHint(Component.literal("플레이어 닉네임"));
                smoothWidget(Button.builder(Component.literal("전화 걸기"), b -> {
                    if (playerName != null && !playerName.getValue().isBlank()) {
                        send("call", playerName.getValue().trim());
                    }
                }).bounds(x - 96, y + 15, 192, 24).build());
            }
        }

        smoothWidget(Button.builder(Component.literal("닫기"), b -> onClose())
                .bounds(x - 96, y + 49, 192, 20).build());
    }

    private float easeIn() {
        float t = Math.min(1.0F, Math.max(0.0F,
                (System.nanoTime() - openedAt) / (float) INTRO_NANOS));
        float inverse = 1.0F - t;
        return 1.0F - inverse * inverse * inverse;
    }

    private static int withOpacity(int rgb, float opacity) {
        int a = Math.round(((rgb >>> 24) & 0xFF) * opacity);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Keep Minecraft visible through its native blurred background.
        this.extractBlurredBackground(g);
        float p = easeIn();
        int x = width / 2 - CARD_WIDTH / 2;
        int y = height / 2 - CARD_HEIGHT / 2 + Math.round(7.0F * (1.0F - p));
        int right = x + CARD_WIDTH;
        int bottom = y + CARD_HEIGHT;

        // Soft shadow, cool slate glass surface, and a restrained sage highlight.
        g.fill(0, 0, width, height, withOpacity(0x180E1820, p));
        g.fill(x - 3, y + 4, right + 3, bottom + 5, withOpacity(0x63000F17, p));
        g.fill(x, y, right, bottom, withOpacity(0xE41B2B35, p));
        g.outline(x, y, CARD_WIDTH, CARD_HEIGHT, withOpacity(0x966D9D98, p));
        g.fill(x + 1, y + 1, x + 4, bottom - 1, withOpacity(0xBC739F95, p));
        g.fill(x + 16, y + 57, right - 16, y + 58, withOpacity(0x68677E82, p));
        g.fill(x + 16, bottom - 36, right - 16, bottom - 35, withOpacity(0x36677E82, p));
    }

    static void send(String action, String target) {
        ClientPlayNetworking.send(new PhoneActionPayload(action, target));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        float p = easeIn();
        for (AnimatedWidget aw : animated) {
            aw.widget().setAlpha(Math.max(0.05F, p));
            aw.widget().setY(aw.baseY() + Math.round(7.0F * (1.0F - p)));
            aw.widget().active = p >= 0.90F;
        }
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = width / 2;
        int y = height / 2 + Math.round(7.0F * (1.0F - p));

        String status = switch (VoicePhoneClient.state) {
            case "incoming" -> "전화가 왔습니다";
            case "outgoing" -> "전화 거는 중...";
            case "active" -> "통화 중";
            case "error" -> "연결할 수 없습니다";
            default -> "전화 걸기";
        };
        g.centeredText(font, "VOICE PHONE", x, y - 83, withOpacity(MINT, p));
        g.centeredText(font, status, x, y - 63, withOpacity(0xFFF3F7F5, p));

        if (!VoicePhoneClient.peer.isBlank()) {
            g.centeredText(font, VoicePhoneClient.peer, x, y - 45, withOpacity(0xFFC1D8D3, p));
        } else {
            g.centeredText(font, "가까운 순간을 연결합니다", x, y - 45, withOpacity(0xFFB3CBC5, p));
        }

        if (!VoicePhoneClient.detail.isBlank()) {
            boolean failed = "error".equals(VoicePhoneClient.state)
                    || VoicePhoneClient.detail.contains("실패")
                    || VoicePhoneClient.detail.contains("없습니다")
                    || VoicePhoneClient.detail.contains("못했습니다");
            int color = failed ? 0xFFFF7676 : 0xFFB7CFC9;
            g.centeredText(font, VoicePhoneClient.detail, x, y + 78, withOpacity(color, p));
        } else {
            g.centeredText(font, "ESC를 누르면 화면이 닫힙니다", x, y + 78,
                    withOpacity(0xFF8CA7A3, p));
        }
    }
}
