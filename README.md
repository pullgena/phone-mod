# Voice Phone — Fabric 26.3 (prototype 0.1.0)

> ⚠️ **소스 코드 프로토타입입니다. 빌드/실행 테스트를 완료한 출시용 JAR가 아닙니다.**
>
> 이 아카이브에는 마인크래프트/Gradle 의존성 파일이나 완성 JAR가 포함되지 않습니다.

## 구현 범위

- 휴대폰(`voicephone:phone`) 아이템, 16×16 텍스처, 조합법
- 우클릭 시 통화 GUI 열기
- 상대 플레이어 이름 입력 → 전화 걸기 → 받기/거절/종료
- Simple Voice Chat API의 **숨김/격리된 그룹**으로 서로 멀리 있어도 양방향 음성 통화
- 동시에 한 통화만 참여, 30초 미응답 타임아웃, 연결 해제/손에서 휴대폰을 내려놓으면 통화 종료
- 다른 보이스챗 그룹에 참가 중인 사용자는 통화에 참가할 수 없음(기존 그룹 보호)

**제한:** 전화번호 체계, 연락처 저장, 벨소리/진동, 배터리, 채팅 메시지, 완전한 휴대폰 모양의 GUI는 아직 없습니다. 이 버전의 전화 대상은 **플레이어 이름**입니다. 근처 사람들에게 통화 음성이 들리지 않도록 `ISOLATED` 그룹을 사용하며, 이때 기존 근접 보이스도 일시적으로 분리됩니다.

## 요구 사항

- Minecraft Java Edition **26.3**
- Fabric Loader(0.19.5 이상; 이 프로젝트의 개발 의존성 버전 0.20.1)
- Fabric API 26.3 버전
- Simple Voice Chat 26.3 버전 (2.6.x 계열)
- Java Development Kit **25**
- 빌드용 Gradle 9.7.1 (또는 GitHub Actions 이용)
- 서버 및 **모든 사용자 클라이언트**에 `voicephone`, Fabric API, Simple Voice Chat 설치
- 서버에서 Simple Voice Chat UDP 포트 연결되어 있어야 함(기본 포트 24454/UDP, 호스트 할당 포트 사용 가능)

## 빌드

1. JDK 25와 Gradle 9.7.1을 설치한 후 프로젝트 폴더를 터미널에서 엽니다.
2. `gradle clean build --no-daemon` 실행.
3. 성공하면 `build/libs/voicephone-fabric-26.3-0.1.0.jar`를 사용합니다. `-sources.jar`는 사용하지 않습니다.
4. 또는 본 프로젝트를 GitHub 저장소에 올리면 **Actions → Build Fabric phone mod** 워크플로가 빌드를 시도하고 결과 JAR를 Artifacts에 업로드합니다.

인터넷 연결이 필요합니다. 최초 빌드 시 Gradle/Loom/Minecraft 파일을 다운로드합니다.

## 게임 테스트

1. 테스트용 Fabric 26.3 서버와 클라이언트 2대를 준비합니다.
2. 각 플레이어에게 `/give @s voicephone:phone`으로 휴대폰을 지급합니다.
3. 양쪽 모두 휴대폰을 손에 든 상태에서 우클릭합니다.
4. A가 B의 **플레이어 이름**을 입력하고 **Call** 선택.
5. B의 화면에 전화가 표시되면 **Answer** 선택.
6. Simple Voice Chat에서 마이크 설정을 완료하면 거리에 상관없이 통화가 전달됩니다.
7. **Decline / End** 버튼으로 종료합니다.

## 개발 및 호환성 주의

- 26.3은 네이밍이 변경된 공식 Mojang 매핑을 사용합니다. 이 프로젝트는 Yarn이 아닌 Loom `net.fabricmc.fabric-loom`을 사용합니다.
- 간혹 최신 26.3 패치에서 API 시그니처가 변경되면 Java 소스 조정이 필요할 수 있습니다.
- `mods` 폴더에 **기존 voicecontrol 등 다른 Simple Voice Chat 애드온**이 있어도 각자 `voicechat` 이벤트를 등록할 수 있습니다. 사용자는 테스트 환경에서 먼저 다른 애드온과 충돌 여부를 확인해 주세요.
- Voice Chat 포트가 통신하지 않으면 이 모드는 음성을 직접 생성/전달할 수 없습니다.

## 참고 자료

- Fabric 26.3: https://fabricmc.net/2026/09/15/263.html
- Fabric 개발 문서: https://docs.fabricmc.net/
- Simple Voice Chat API: https://dev.modrepo.de/minecraft/voicechat/api/getting_started
- Simple Voice Chat GitHub 26.3 API: https://github.com/henkelmax/simple-voice-chat/tree/26.3/api
