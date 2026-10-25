package kr.yehwon.voicephone.update;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.*;

public final class PhoneUpdater {
 private static final String API="https://api.github.com/repos/pullgena/phone-mod/releases/latest";
 private static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build();
 private static final ExecutorService EXEC=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"VoicePhone-updater");t.setDaemon(true);return t;});
 private static final AtomicBoolean RUNNING=new AtomicBoolean(),STARTED=new AtomicBoolean();
 private static volatile String message="업데이트 확인 전";
 private PhoneUpdater(){}
 public static String status(){return message;}
 public static void start(){
  if(STARTED.compareAndSet(false,true)){
   Thread t=new Thread(()->{
    try{Thread.sleep(5000);}catch(InterruptedException e){Thread.currentThread().interrupt();return;}
    checkNow();
    while(true){try{Thread.sleep(3600000);}catch(InterruptedException e){return;}checkNow();}
   },"VoicePhone-update-timer");t.setDaemon(true);t.start();
  }
 }
 public static boolean checkNow(){
  if(!RUNNING.compareAndSet(false,true))return false;
  EXEC.execute(()->{try{check();}catch(Exception e){message="업데이트 실패: "+e.getMessage();LoggerFactory.getLogger("voicephone-updater").warn(message,e);}finally{RUNNING.set(false);}});
  return true;
 }
 private static void check()throws Exception{
  var mod=FabricLoader.getInstance().getModContainer("voicephone").orElseThrow();
  String current=mod.getMetadata().getVersion().getFriendlyString();
  if(!current.matches("\\d+\\.\\d+\\.\\d+")){message="개발 빌드의 자동 업데이트는 지원하지 않습니다";return;}
  if(mod.getOrigin().getKind()!=ModOrigin.Kind.PATH||mod.getOrigin().getPaths().size()!=1){message="설치된 JAR를 찾지 못했습니다";return;}
  Path game=FabricLoader.getInstance().getGameDir().toAbsolutePath().normalize(),old=mod.getOrigin().getPaths().get(0).toAbsolutePath().normalize();
  if(!old.startsWith(game.resolve("mods"))||!Files.isRegularFile(old)){message="mods 폴더의 JAR만 업데이트할 수 있습니다";return;}
  var request=HttpRequest.newBuilder(URI.create(API)).timeout(Duration.ofSeconds(30)).header("User-Agent","VoicePhone").GET().build();
  var response=HTTP.send(request,HttpResponse.BodyHandlers.ofString());
  if(response.statusCode()==404){message="GitHub 최신 Release가 없습니다";return;}
  if(response.statusCode()!=200)throw new IOException("GitHub HTTP "+response.statusCode());
  JsonObject release=JsonParser.parseString(response.body()).getAsJsonObject();
  String tag=release.get("tag_name").getAsString(),latest=tag.startsWith("v")?tag.substring(1):tag;
  if(!latest.matches("\\d+\\.\\d+\\.\\d+"))throw new IOException("Release 버전 형식 오류");
  int[] a=Arrays.stream(latest.split("\\.")).mapToInt(Integer::parseInt).toArray();
  int[] b=Arrays.stream(current.split("\\.")).mapToInt(Integer::parseInt).toArray();
  boolean newer=false;for(int i=0;i<3;i++){if(a[i]>b[i]){newer=true;break;}if(a[i]<b[i])break;}
  if(!newer){message="최신 버전: "+current;return;}
  String name="voicephone-fabric-26.3-"+latest+".jar";
  JsonObject asset=null;
  for(var item:release.getAsJsonArray("assets")){var x=item.getAsJsonObject();if(name.equals(x.get("name").getAsString()))asset=x;}
  if(asset==null){message="릴리스에 "+name+" 파일 없음";return;}
  String hash=asset.has("digest")&&!asset.get("digest").isJsonNull()?asset.get("digest").getAsString():"";
  if(!hash.matches("sha256:[0-9a-fA-F]{64}"))throw new IOException("GitHub SHA-256 검증값이 없습니다");
  URI uri=URI.create(asset.get("browser_download_url").getAsString());
  if(!"https".equals(uri.getScheme())||!"github.com".equals(uri.getHost()))throw new IOException("잘못된 다운로드 URL");
  message=latest+" 다운로드 중...";
  Path folder=game.resolve("config/voicephone-updates");Files.createDirectories(folder);
  Path staged=folder.resolve(name),temporary=folder.resolve(name+".part");
  try{
   var res=HTTP.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(60)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
   if(res.statusCode()!=200||res.body().length>32*1024*1024)throw new IOException("릴리스 다운로드 실패/크기 초과");
   MessageDigest digest=MessageDigest.getInstance("SHA-256");
   if(!HexFormat.of().formatHex(digest.digest(res.body())).equalsIgnoreCase(hash.substring(7)))throw new IOException("SHA-256 무결성 확인 실패");
   Files.write(temporary,res.body());
   try(JarFile jar=new JarFile(temporary.toFile())){
    var entry=jar.getJarEntry("fabric.mod.json");if(entry==null)throw new IOException("Fabric 모드 파일 없음");
    try(InputStream in=jar.getInputStream(entry)){
     var metadata=JsonParser.parseString(new String(in.readNBytes(65536),StandardCharsets.UTF_8)).getAsJsonObject();
     if(!"voicephone".equals(metadata.get("id").getAsString())||!latest.equals(metadata.get("version").getAsString()))throw new IOException("JAR 모드 ID/버전 불일치");
    }
   }
   Files.move(temporary,staged,StandardCopyOption.REPLACE_EXISTING);
   Path script=folder.resolve("update-"+latest+(System.getProperty("os.name").toLowerCase().contains("win")?".bat":".sh"));
   if(System.getProperty("os.name").toLowerCase().contains("win")){
    Files.writeString(script,"@echo off\r\n:retry\r\nmove /Y \""+old+"\" \""+folder.resolve("backup-"+current+".jar")+"\" >nul 2>&1\r\nif errorlevel 1 (timeout /t 2 /nobreak >nul & goto retry)\r\nmove /Y \""+staged+"\" \""+old.getParent().resolve(name)+"\" >nul\r\n");
   }else{
    Files.writeString(script,"#!/bin/sh\nwhile ! mv '"+old+"' '"+folder.resolve("backup-"+current+".jar")+"' 2>/dev/null; do sleep 2; done\nmv '"+staged+"' '"+old.getParent().resolve(name)+"'\n");
   }
   message="v"+latest+" 다운로드 완료. config/voicephone-updates의 갱신 스크립트를 실행하고 Minecraft를 종료한 후 재시작하세요.";
  }finally{Files.deleteIfExists(temporary);}
 }
}
