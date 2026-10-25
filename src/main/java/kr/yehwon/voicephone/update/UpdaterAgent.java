package kr.yehwon.voicephone.update;
import java.nio.file.*;
public final class UpdaterAgent {
 private UpdaterAgent(){}
 public static void main(String[] args) throws Exception {
  if(args.length!=5) throw new IllegalArgumentException("pid original staged destination backup");
  long pid=Long.parseLong(args[0]);
  Path original=Path.of(args[1]), staged=Path.of(args[2]), dest=Path.of(args[3]), backup=Path.of(args[4]);
  ProcessHandle.of(pid).ifPresent(p->{if(p.isAlive())p.onExit().join();});
  if(!Files.isRegularFile(staged)) throw new IllegalStateException("Update download missing");
  Files.createDirectories(backup.getParent());
  for(int i=0;i<120;i++){
   try{
    if(!Files.isRegularFile(original))throw new IllegalStateException("Original mod JAR missing");
    Files.move(original,backup,StandardCopyOption.REPLACE_EXISTING);
    try{
     Files.move(staged,dest,StandardCopyOption.REPLACE_EXISTING);
     System.out.println("VoicePhone updated to "+dest);
     return;
    }catch(Exception e){
     Files.move(backup,original,StandardCopyOption.REPLACE_EXISTING);
     throw e;
    }
   }catch(Exception e){
    if(i==119)throw e;
    Thread.sleep(1000);
   }
  }
 }
}
