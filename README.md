# Voice Phone — Fabric 26.3 (prototype 0.4.1)

> ⚠️ **개발 중인 버전으로 실제 멀티플레이 통화 및 릴리스 업데이트 테스트가 필요합니다.**
>
> 이 아카이브에는 마인크래프트/Gradle 의존성 파일이나 완성 JAR가 포함되지 않습니다.

## 구현 범위

- 휴대폰(`voicephone:phone`) 아이템, 16×16 텍스처, 조합법
- 우클릭 시 통화 GUI 열기
- 상대 플레이어 이름 입력 → 전화 걸기 → 받기/거절/종료
- Simple Voice Chat API의 **숨김/열림(OPEN) 그룹**으로 서로 멀리 있어도 양방향 음성 통화
- 동시에 한 통화만 참여, 30초 미응답 타임아웃, 연결 해제/손에서 휴대폰을 내려놓으면 통화 종료
- 다른 보이스챗 그룹에 참가 중인 사용자는 통화에 참가할 수 없음(기존 그룹 보호)

**제한:** 전화번호 체계, 연락처 저장, 벨소리/진동, 배터리, 채팅 메시지, 완전한 휴대폰 모양의 GUI는 아직 없습니다. 이 버전의 전화 대상은 **플레이어 이름**입니다. `OPEN` 그룹을 사용하여 주변 사람과 근접 음성도 주고받도록 설정합니다. 다만 멀리 있는 상대방의 통화 음성이 주변 사람에게 휴대폰 스피커처럼 전달되는 것은 별도의 음성 중계 구현이 필요할 수 있습니다.

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
3. 성공하면 `build/libs/voicephone-fabric-26.3-0.4.1.jar`를 사용합니다. `-sources.jar`는 사용하지 않습니다.
4. 또는 본 프로젝트를 GitHub 저장소에 올리면 **Actions → Build Fabric phone mod** 워크플로가 빌드를 시도하고 결과 JAR를 Artifacts에 업로드합니다.

인터넷 연결이 필요합니다. 최초 빌드 시 Gradle/Loom/Minecraft 파일을 다운로드합니다.

## 게임 테스트

1. 테스트용 Fabric 26.3 서버와 클라이언트 2대를 준비합니다.
2. 각 플레이어에게 `/give @s voicephone:phone`으로 휴대폰을 지급합니다.
3. 발신자는 휴대폰을 손에 든 상태에서 우클릭합니다. 수신자는 인벤토리에 휴대폰을 가지고 있으면 됩니다.
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

## UI와 자동 업데이트 (v0.3.0)

- 대기 상태: 닉네임 입력 및 **전화 걸기**만 표시
- 수신 상태: **받기/거절**만 표시
- 발신 대기: **전화 취소**만 표시
- 통화 중: **통화 종료**만 표시
- 통화 상태가 바뀌면 UI를 즉시 새로고침, ESC로 닫기
- OP/콘솔: `/phoneupdate`, `/phoneupdate status`, `/phoneupdate check`
- GitHub Release(vX.Y.Z)에 호환 JAR 업로드 시 주기적으로 확인하며 SHA-256 검증 후 다운로드
- 전용 Fabric 서버(Pterodactyl 포함)는 서버가 **정상 종료될 때** JAR를 교체하고 다음 시작부터 적용합니다. 서버 강제 종료/호스팅의 파일 쓰기 제한에서는 적용되지 않을 수 있습니다.
- PC 클라이언트는 Minecraft 종료 후 보조 프로세스를 통해 교체를 시도합니다. 서버와 클라이언트 버전이 일치해야 통신 오류를 피할 수 있습니다.
- 릴리스 업데이트 기능이 없던 구버전에서의 최초 업그레이드는 수동으로 필요합니다.

## v0.4.0: 전화 수신 / 메인 메뉴 업데이트

- 수신자는 휴대폰을 손에 들지 않아도 **인벤토리에 가지고 있으면** 전화 받을 수 있습니다.
- 수신자가 휴대폰을 들고 있으면 전체 화면 전화 UI, 인벤토리에만 있으면 작은 전화 팝업을 표시합니다.
- 통화 중에도 수신자는 휴대폰을 들지 않아도 됩니다. 발신자는 기존처럼 휴대폰을 들어야 합니다.
- 메인 메뉴 우측 상단의 **Voice Phone 업데이트**를 눌러 버전 확인 및 다운로드할 수 있습니다.
- 싱글플레이 월드를 열지 않아도 클라이언트에서 독립적으로 GitHub Releases를 확인할 수 있습니다.
- 파일 교체는 Minecraft를 종료한 뒤 진행되며 서버는 정상 종료 시 준비된 업데이트를 적용합니다.
- 현재 수신 팝업은 게임 화면 위에 표시되는 간소화된 모달 창입니다. 전화 받기/거절 버튼을 누르거나 ESC로 닫을 수 있습니다.

## v0.4.1: Open voice chat group

- 전화 연결 시 Voice Chat 그룹 형식을 `Group.Type.OPEN`으로 사용합니다.
- 그룹의 사용자는 주변 플레이어 음성을 듣고, 주변 플레이어도 그룹 구성원의 근접 음성을 들을 수 있습니다.
- 멀리 떨어진 통화 상대의 음성을 휴대폰 스피커처럼 주변에 재송출하는 기능과는 별개입니다.
- 서버 및 클라이언트 모두 동일한 v0.4.1 JAR로 업데이트한 뒤 재시작해야 합니다.
