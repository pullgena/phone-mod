package kr.yehwon.voicephone.client;

import kr.yehwon.voicephone.net.PhoneActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Display only the actions relevant to the current call state. */
public final class PhoneScreen extends Screen {
    static PhoneScreen visible;
    private EditBox playerName;

    public PhoneScreen() {
        super(Component.literal("Voice Phone"));
    }

    @Override
    protected void init() {
        visible = this;
        int x = width / 2;
        int y = height / 2;
        String state = VoicePhoneClient.state;
        switch (state) {
            case "incoming" -> {
                addRenderableWidget(Button.builder(Component.literal("받기"), button -> send("accept", ""))
                        .bounds(x - 82, y + 15, 78, 22).build());
                addRenderableWidget(Button.builder(Component.literal("거절"), button -> send("reject", ""))
                        .bounds(x + 4, y + 15, 78, 22).build());
            }
            case "outgoing" ->
                addRenderableWidget(Button.builder(Component.literal("전화 취소"), button -> send("hangup", ""))
                        .bounds(x - 82, y + 15, 164, 22).build());
            case "active" ->
                addRenderableWidget(Button.builder(Component.literal("통화 종료"), button -> send("hangup", ""))
                        .bounds(x - 82, y + 15, 164, 22).build());
            default -> {
                playerName = new EditBox(this.font, x - 82, y - 12, 164, 20,
                        Component.literal("상대 플레이어 닉네임"));
                playerName.setMaxLength(16);
                playerName.setHint(Component.literal("플레이어 닉네임"));
                addRenderableWidget(playerName);
                addRenderableWidget(Button.builder(Component.literal("전화 걸기"), button -> {
                    if (playerName != null && !playerName.getValue().isBlank()) {
                        send("call", playerName.getValue().trim());
                    }
                }).bounds(x - 82, y + 15, 164, 22).build());
            }
        }
    }

    @Override
    public void onClose() {
        if (visible == this) visible = null;
        super.onClose();
    }

    private static void send(String action, String target) {
        ClientPlayNetworking.send(new PhoneActionPayload(action, target));
    }

    private static String titleFor(String state) {
        return switch (state) {
            case "incoming" -> "전화가 왔습니다";
            case "outgoing" -> "전화 거는 중...";
            case "active" -> "통화 중";
            default -> "전화 걸기";
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int x = width / 2;
        int y = height / 2;
        String state = VoicePhoneClient.state;
        graphics.text(this.font, "VOICE PHONE", x - 33, y - 67, 0xFF92D6FF, true);
        graphics.text(this.font, titleFor(state), x - 82, y - 46, 0xFFFFFFFF, true);
        if (!VoicePhoneClient.peer.isBlank()) {
            graphics.text(this.font, VoicePhoneClient.peer, x - 82, y - 29, 0xFFEAEAEA, true);
        }
        if (!VoicePhoneClient.detail.isBlank()) {
            graphics.text(this.font, VoicePhoneClient.detail, x - 82, y + 46, 0xFFDDDDDD, true);
        }
        graphics.text(this.font, "ESC: 닫기", x - 82, y + 67, 0xFF9E9E9E, false);
    }
}
