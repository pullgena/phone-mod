package kr.yehwon.voicephone.client;

import kr.yehwon.voicephone.net.PhoneActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PhoneScreen extends Screen {
    private EditBox playerName;

    public PhoneScreen() {
        super(Component.literal("Voice Phone"));
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2;
        this.playerName = new EditBox(this.font, x - 78, y - 41, 156, 20,
                Component.literal("플레이어 이름"));
        this.playerName.setMaxLength(16);
        this.playerName.setHint(Component.literal("상대 닉네임"));
        addRenderableWidget(playerName);
        addRenderableWidget(Button.builder(Component.literal("전화 걸기"), b -> send("call", playerName.getValue()))
                .bounds(x - 78, y - 14, 156, 20).build());
        addRenderableWidget(Button.builder(Component.literal("전화 받기"), b -> send("accept", ""))
                .bounds(x - 78, y + 14, 75, 20).build());
        addRenderableWidget(Button.builder(Component.literal("끊기 / 거절"), b -> send("hangup", ""))
                .bounds(x + 3, y + 14, 75, 20).build());
        addRenderableWidget(Button.builder(Component.literal("닫기"), b -> onClose())
                .bounds(x - 78, y + 43, 156, 20).build());
    }

    private static void send(String action, String target) {
        ClientPlayNetworking.send(new PhoneActionPayload(action, target));
    }

    private static String translateState(String status) {
        return switch (status) {
            case "idle" -> "대기 중";
            case "outgoing" -> "연결 요청 중";
            case "incoming" -> "전화가 왔습니다";
            case "active" -> "통화 중";
            case "error" -> "알림";
            default -> status;
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int x = this.width / 2;
        int y = this.height / 2;
        graphics.text(this.font, "VOICE PHONE", x - 39, y - 84, 0xFF96D9FF, true);
        graphics.text(this.font, "상태: " + translateState(VoicePhoneClient.state),
                x - 78, y - 72, 0xFFFFFFFF, true);
        if (!VoicePhoneClient.peer.isEmpty()) {
            graphics.text(this.font, "상대: " + VoicePhoneClient.peer,
                    x - 78, y - 59, 0xFFFFFFFF, true);
        }
        if (!VoicePhoneClient.detail.isEmpty()) {
            graphics.text(this.font, VoicePhoneClient.detail,
                    x - 78, y + 72, 0xFFDDDDDD, true);
        }
    }
}
