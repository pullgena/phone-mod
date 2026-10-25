package kr.yehwon.voicephone.client;

import kr.yehwon.voicephone.net.PhoneActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Full-screen phone when the item is held. Controls are state-specific. */
public final class PhoneScreen extends Screen {
    private EditBox playerName;

    public PhoneScreen() {
        super(Component.literal("Voice Phone"));
    }

    @Override
    protected void init() {
        int x = width / 2;
        int y = height / 2;
        String state = VoicePhoneClient.state;
        switch (state) {
            case "incoming" -> {
                addRenderableWidget(Button.builder(Component.literal("받기"), b -> send("accept", ""))
                        .bounds(x - 102, y + 40, 98, 24).build());
                addRenderableWidget(Button.builder(Component.literal("거절"), b -> send("reject", ""))
                        .bounds(x + 4, y + 40, 98, 24).build());
            }
            case "outgoing" -> addRenderableWidget(Button.builder(Component.literal("전화 취소"), b -> send("hangup", ""))
                    .bounds(x - 102, y + 40, 204, 24).build());
            case "active" -> addRenderableWidget(Button.builder(Component.literal("통화 종료"), b -> send("hangup", ""))
                    .bounds(x - 102, y + 40, 204, 24).build());
            default -> {
                playerName = new EditBox(font, x - 102, y + 2, 204, 22, Component.literal("상대 플레이어"));
                playerName.setMaxLength(16);
                playerName.setHint(Component.literal("플레이어 닉네임"));
                addRenderableWidget(playerName);
                addRenderableWidget(Button.builder(Component.literal("전화 걸기"), b -> {
                    if (playerName != null && !playerName.getValue().isBlank()) {
                        send("call", playerName.getValue().trim());
                    }
                }).bounds(x - 102, y + 40, 204, 24).build());
            }
        }
        addRenderableWidget(Button.builder(Component.literal("닫기"), b -> onClose())
                .bounds(x - 102, height - 43, 204, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Apply Minecraft 26.3's native menu blur to the scene behind the phone UI.
        // Unlike a full-screen gradient, this leaves the world recognizable.
        // Respects the player's Menu Background Blurriness setting.
        this.extractBlurredBackground(g);
        g.fill(0, 0, width, height, 0x18000000);
    }

    static void send(String action, String target) {
        ClientPlayNetworking.send(new PhoneActionPayload(action, target));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = width / 2;
        int y = height / 2;
        g.centeredText(font, "VOICE PHONE", x, 17, 0xFFA4D7FF);
        String status = switch (VoicePhoneClient.state) {
            case "incoming" -> "전화가 왔습니다";
            case "outgoing" -> "전화 거는 중...";
            case "active" -> "통화 중";
            default -> "전화 걸기";
        };
        g.centeredText(font, status, x, y - 86, 0xFFFFFFFF);
        if (!VoicePhoneClient.peer.isBlank()) {
            g.centeredText(font, VoicePhoneClient.peer, x, y - 55, 0xFFA2C9F2);
        }
        if (!VoicePhoneClient.detail.isBlank()) {
            g.centeredText(font, VoicePhoneClient.detail, x, y + 82, ("error".equals(VoicePhoneClient.state) || VoicePhoneClient.detail.contains("실패") || VoicePhoneClient.detail.contains("없습니다") || VoicePhoneClient.detail.contains("못했습니다") ? 0xFFFF5555 : 0xFFC6D2DF));
        }
    }
}
