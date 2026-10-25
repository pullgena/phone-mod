package kr.yehwon.voicephone.update;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
public final class PhoneUpdateCommands{
 private PhoneUpdateCommands(){}
 public static void register(){
  CommandRegistrationCallback.EVENT.register((dispatcher,lookup,environment)->dispatcher.register(
   Commands.literal("phoneupdate").requires(s->s.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
    .executes(ctx->{ctx.getSource().sendSuccess(()->Component.literal(PhoneUpdater.status()),false);return 1;})
    .then(Commands.literal("status").executes(ctx->{ctx.getSource().sendSuccess(()->Component.literal(PhoneUpdater.status()),false);return 1;}))
    .then(Commands.literal("check").executes(ctx->{boolean ok=PhoneUpdater.checkNow();ctx.getSource().sendSuccess(()->Component.literal(ok?"업데이트 확인 시작":PhoneUpdater.status()),false);return 1;}))
  ));
 }
}
