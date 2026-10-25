package kr.yehwon.voicephone;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;

public final class PhoneVoicePlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return VoicePhone.MOD_ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class,
                event -> PhoneManager.voiceApi = event.getVoicechat());
        registration.registerEvent(VoicechatServerStoppedEvent.class,
                event -> PhoneManager.voiceApi = null);
    }
}
