package kr.yehwon.voicephone;

import kr.yehwon.voicephone.net.PhoneActionPayload;
import kr.yehwon.voicephone.net.PhoneStatePayload;
import net.fabricmc.api.ModInitializer;
import kr.yehwon.voicephone.update.PhoneUpdater;
import kr.yehwon.voicephone.update.PhoneUpdateCommands;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;

public final class VoicePhone implements ModInitializer {
    public static final String MOD_ID = "voicephone";
    private static final ResourceKey<Item> PHONE_KEY = ResourceKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "phone"));
    public static final Item PHONE = Registry.register(BuiltInRegistries.ITEM, PHONE_KEY,
            new PhoneItem(new Item.Properties().setId(PHONE_KEY).stacksTo(1)));

    @Override
    public void onInitialize() {
        PhoneUpdateCommands.register();
        PhoneUpdater.start();
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PhoneUpdater.applyOnDedicatedServerStop());
        PayloadTypeRegistry.serverboundPlay().register(PhoneActionPayload.TYPE, PhoneActionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PhoneStatePayload.TYPE, PhoneStatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PhoneActionPayload.TYPE, (payload, context) ->
                PhoneManager.handle(context.player(), context.server(), payload));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PhoneManager.onDisconnect(handler.player, server));
        ServerTickEvents.END_SERVER_TICK.register(PhoneManager::tick);
    }
}
