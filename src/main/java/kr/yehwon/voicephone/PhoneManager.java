package kr.yehwon.voicephone;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import kr.yehwon.voicephone.net.PhoneActionPayload;
import kr.yehwon.voicephone.net.PhoneStatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** All decisions are server-authoritative. Only the server's game thread edits SESSIONS. */
public final class PhoneManager {
    private PhoneManager() {}

    public static volatile VoicechatServerApi voiceApi;
    private static final Map<UUID, Call> SESSIONS = new HashMap<>();
    private static final long TIMEOUT_TICKS = 20L * 30L;

    private static final class Call {
        final UUID caller;
        final UUID callee;
        final long created;
        boolean active;
        Group group;

        Call(UUID caller, UUID callee, long created) {
            this.caller = caller;
            this.callee = callee;
            this.created = created;
        }
        UUID other(UUID self) { return caller.equals(self) ? callee : caller; }
    }

    public static void open(ServerPlayer player) {
        Call call = SESSIONS.get(player.getUUID());
        if (call == null) {
            notify(player, "idle", "", "상대 플레이어 이름을 입력하세요", true);
        } else {
            boolean incoming = !call.active && call.callee.equals(player.getUUID());
            String state = call.active ? "active" : (incoming ? "incoming" : "outgoing");
            ServerPlayer other = player.getServer().getPlayerList().getPlayer(call.other(player.getUUID()));
            notify(player, state, other == null ? "?" : other.getPlainTextName(), "", true);
        }
    }

    public static void handle(ServerPlayer sender, MinecraftServer server, PhoneActionPayload request) {
        if (!holding(sender)) {
            notify(sender, "error", "", "휴대폰을 손에 들어주세요", false);
            return;
        }
        switch (request.action()) {
            case "call" -> call(sender, server, request.target());
            case "accept" -> accept(sender, server);
            case "reject", "hangup" -> hangup(sender, server);
            default -> notify(sender, "error", "", "지원하지 않는 동작입니다", false);
        }
    }

    private static void call(ServerPlayer sender, MinecraftServer server, String playerName) {
        if (playerName.length() < 1 || playerName.length() > 16 || !playerName.matches("[A-Za-z0-9_]+")) {
            notify(sender, "error", "", "올바른 닉네임을 입력하세요", false);
            return;
        }
        if (SESSIONS.containsKey(sender.getUUID())) {
            notify(sender, "error", "", "진행 중인 전화를 먼저 종료하세요", false);
            return;
        }
        ServerPlayer target = server.getPlayerList().getPlayerByName(playerName);
        if (target == null || target.getUUID().equals(sender.getUUID()) || !holding(target)) {
            notify(sender, "error", "", "상대가 접속하지 않았거나 휴대폰을 들고 있지 않습니다", false);
            return;
        }
        if (SESSIONS.containsKey(target.getUUID())) {
            notify(sender, "error", "", "상대가 통화 중입니다", false);
            return;
        }
        if (!available(sender) || !available(target)) {
            notify(sender, "error", "", "양쪽 모두 보이스챗에 연결되어야 합니다", false);
            return;
        }
        if (inGroup(sender) || inGroup(target)) {
            notify(sender, "error", "", "기존 보이스챗 그룹에서 나와주세요", false);
            return;
        }
        Call session = new Call(sender.getUUID(), target.getUUID(), server.getTickCount());
        SESSIONS.put(sender.getUUID(), session);
        SESSIONS.put(target.getUUID(), session);
        notify(sender, "outgoing", target.getPlainTextName(), "전화 연결 중...", false);
        notify(target, "incoming", sender.getPlainTextName(), "전화가 왔습니다", true);
    }

    private static void accept(ServerPlayer receiver, MinecraftServer server) {
        Call session = SESSIONS.get(receiver.getUUID());
        if (session == null || session.active || !session.callee.equals(receiver.getUUID())) {
            notify(receiver, "error", "", "받을 전화가 없습니다", false);
            return;
        }
        ServerPlayer caller = server.getPlayerList().getPlayer(session.caller);
        if (caller == null || !holding(caller) || !available(caller) || !available(receiver)
                || inGroup(caller) || inGroup(receiver)) {
            finish(session, server, "연결할 수 없는 전화입니다");
            return;
        }
        try {
            Group group = voiceApi.groupBuilder()
                    .setName("Phone-" + caller.getPlainTextName())
                    .setType(Group.Type.ISOLATED)
                    .setHidden(true)
                    .setPersistent(false)
                    .build();
            if (group == null) {
                finish(session, server, "통화 그룹을 생성하지 못했습니다");
                return;
            }
            session.group = group;
            voiceApi.getConnectionOf(caller.getUUID()).setGroup(group);
            voiceApi.getConnectionOf(receiver.getUUID()).setGroup(group);
            session.active = true;
            notify(caller, "active", receiver.getPlainTextName(), "통화 연결됨", false);
            notify(receiver, "active", caller.getPlainTextName(), "통화 연결됨", false);
        } catch (RuntimeException e) {
            finish(session, server, "보이스챗 연결 실패");
        }
    }

    private static void hangup(ServerPlayer sender, MinecraftServer server) {
        Call session = SESSIONS.get(sender.getUUID());
        if (session == null) {
            notify(sender, "idle", "", "통화 중이 아닙니다", false);
            return;
        }
        finish(session, server, "통화를 종료했습니다");
    }

    private static void finish(Call session, MinecraftServer server, String detail) {
        if (SESSIONS.get(session.caller) != session || SESSIONS.get(session.callee) != session) return;
        SESSIONS.remove(session.caller);
        SESSIONS.remove(session.callee);
        if (session.group != null && voiceApi != null) {
            for (UUID id : new UUID[] {session.caller, session.callee}) {
                VoicechatConnection conn = voiceApi.getConnectionOf(id);
                if (conn != null && conn.getGroup() != null
                        && conn.getGroup().getId().equals(session.group.getId())) {
                    conn.setGroup(null);
                }
            }
        }
        ServerPlayer a = server.getPlayerList().getPlayer(session.caller);
        ServerPlayer b = server.getPlayerList().getPlayer(session.callee);
        if (a != null) notify(a, "idle", "", detail, false);
        if (b != null) notify(b, "idle", "", detail, false);
    }

    public static void onDisconnect(ServerPlayer player, MinecraftServer server) {
        Call call = SESSIONS.get(player.getUUID());
        if (call != null) finish(call, server, "상대 플레이어의 접속이 종료되었습니다");
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        Set<Call> unique = new HashSet<>(SESSIONS.values());
        for (Call c : unique) {
            ServerPlayer a = server.getPlayerList().getPlayer(c.caller);
            ServerPlayer b = server.getPlayerList().getPlayer(c.callee);
            if (a == null || b == null || !holding(a) || !holding(b)) {
                finish(c, server, "휴대폰을 손에서 내려놓았습니다");
            } else if (!available(a) || !available(b)) {
                finish(c, server, "보이스챗 연결이 끊어졌습니다");
            } else if (!c.active && server.getTickCount() - c.created >= TIMEOUT_TICKS) {
                finish(c, server, "30초 동안 받지 않아 종료했습니다");
            } else if (c.active && (voiceApi == null || c.group == null
                    || !inSameGroup(a, c.group) || !inSameGroup(b, c.group))) {
                finish(c, server, "통화 그룹이 변경되어 종료했습니다");
            }
        }
    }

    private static boolean available(ServerPlayer player) {
        if (voiceApi == null) return false;
        VoicechatConnection conn = voiceApi.getConnectionOf(player.getUUID());
        return conn != null && conn.isInstalled() && conn.isConnected();
    }

    private static boolean inGroup(ServerPlayer player) {
        VoicechatConnection connection = voiceApi == null ? null : voiceApi.getConnectionOf(player.getUUID());
        return connection != null && connection.isInGroup();
    }

    private static boolean inSameGroup(ServerPlayer player, Group group) {
        VoicechatConnection conn = voiceApi.getConnectionOf(player.getUUID());
        return conn != null && conn.getGroup() != null && conn.getGroup().getId().equals(group.getId());
    }

    private static boolean holding(ServerPlayer player) {
        return player.getMainHandItem().is(VoicePhone.PHONE) || player.getOffhandItem().is(VoicePhone.PHONE);
    }

    private static void notify(ServerPlayer player, String state, String peer, String detail, boolean open) {
        if (ServerPlayNetworking.canSend(player, PhoneStatePayload.TYPE)) {
            ServerPlayNetworking.send(player, new PhoneStatePayload(state, peer, detail, open));
        }
    }
}
