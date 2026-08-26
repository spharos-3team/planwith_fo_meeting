# Meeting API 진행 현황

> 이슈 완료마다 이 문서를 갱신한다.  
> Swagger (로컬 bootRun): `http://localhost:8086/swagger-ui/index.html`  
> Swagger (다른 PC / Docker): `http://{SERVER_IP}:8000/swagger-ui.html` → `planwith-fo-meeting`  
> 공통 응답: `ApiResponse<T>`  
> 호출 경로: `Frontend → Gateway(:8000) → Meeting(:8086)` (Access 검증은 Gateway)

최종 갱신: 2026-08-21 (#11 채팅 연동 이벤트 발행)

---

## 서비스 경계

`planwith-fo-meeting` 은 **모임 + 참여/신청 + 구성원 역할** 만 담당한다.  
채팅은 **완전 분리**된 채팅 서비스가 소유한다. 모임 DB에 `chat_rooms` / `chat_messages` 를 두지 않는다.

| 요구사항 묶음 | 담당 |
| --- | --- |
| 모임 CRUD, 목록/상세, 모집상태, 끌어올리기, 완료/해체 | **meeting** |
| 신청/승인/거절, 구성원, 부방장, 강퇴, 탈퇴 | **meeting** |
| 채팅방·멤버·읽음·알림 (`chat_rooms`, `chat_members` MySQL) | **chat** |
| 메시지 (`chat_messages` MongoDB) | **chat** |
| 일정 원본 | schedule. 목록 카드용 목적지·기간만 meeting이 생성 시 스냅샷 |
| 프로필·팔로우 | member |
| 끌어올리기 등급 | grade |
| 스토리 / 토큰 / 멤버십 / 결제 | story / token / grade·payment |

OpenFeign 금지. 서비스 간은 Kafka `EventEnvelope<T>` (또는 Gateway Trust HTTP). 채팅 테이블을 meeting이 직접 INSERT/DELETE 하지 않는다.

### 모임 ↔ 채팅 생명주기

같은 트랜잭션이 아니다. meeting이 이벤트를 내고 chat이 자기 DB를 맞춘다.

```
모임 생성 성공  →  meeting.created     →  chat: chat_rooms INSERT (ACTIVE)
                                        + 호스트 chat_members APPROVED
모임 완료        →  meeting.completed   →  chat: chat_rooms.status = ENDED (입력 차단, 방 유지)
모임 해체(삭제)  →  meeting.disbanded   →  chat: chat_rooms·chat_members 삭제
                                        + Mongo chat_messages 삭제
참여 상태 변경   →  meeting.participation.changed
                  PENDING / APPROVED / REJECTED / LEFT / KICKED
                →  chat_members.status 동기화 (승인 시 joined_at)
```

chat 스키마 (chat 서비스, meeting 아님):

- MySQL `chat_rooms`: `chat_room_uuid`, `meeting_uuid` UNIQUE, `status` `ACTIVE|ENDED`
- MySQL `chat_members`: `last_read_message_uuid`, `notification_enabled`, `status` `PENDING|APPROVED|REJECTED|LEFT|KICKED`, `joined_at`
- Mongo `chat_messages`: `messageUuid`, `chatRoomUuid`, `senderUuid`, `messageType`, `content`, `files[]` (`FileType`: IMAGE/VIDEO/AUDIO/DOCUMENT/ETC), `isModified`, `isDeleted`, `createdAt`, `updatedAt`

---

## 컨벤션

- Prefix: `/api/v1` (템플릿 `/api/planwith-fo-meeting` 쓰지 않음)
- Gateway Path (이 서비스): `/api/v1/meetings/**` 만. 채팅 Path는 chat 서비스
- 외부 식별자: `*Uuid` (char 36). PK는 내부용
- 시간: UTC. 응답 Instant
- 인증 API: Gateway `X-Auth-User-Id` → `AuthenticatedUserContext` (헤더 직접 파싱 금지)
- 비회원 허용: 전체 모임 목록(카드)·상세. 로그인 시 **상세에만** `myParticipation` 포함
- 헥사고날: `adapter → application → domain` (`meeting` / `participation`). **chat 도메인 없음**
- Entity를 API 응답으로 반환하지 않음
- 페이지: `page` 기본 0, `size` 기본 20, 최대 50 (`PagedResponse`). 목록은 전량 조회하지 않음
- 상세 화면의 일정 인원·예상 비용·이동수단은 `scheduleUuid`로 **schedule 서비스**가 반환
- 목록 카드의 목적지·여행 기간은 생성 시 meetings에 스냅샷 저장. 목록은 meeting API만 호출

### 상태

| 구분 | 값 |
| --- | --- |
| 모임 | `RECRUITING` 모집중 / `FULL` 모집완료(정원참) / `COMPLETED` 완료 / `DISBANDED` 해체 |
| 참여 | `PENDING` 신청대기 / `APPROVED` 참여 / `REJECTED` 거절 / `LEFT` 탈퇴 / `KICKED` 강퇴 |
| 역할 (meeting) | `HOST` / `VICE_HOST` / `MEMBER` |
| 채팅방 (chat) | `ACTIVE` / `ENDED` |
| 채팅 멤버 (chat) | `PENDING` / `APPROVED` / `REJECTED` / `LEFT` / `KICKED` |

참여 `APPROVED` → 채팅 멤버 `APPROVED` 로 매핑한다.

규칙:

- 생성 시 `meetings.member_uuid`=생성자, 모임=`RECRUITING`, 호스트 `meeting_members`=`APPROVED`+`HOST`. 채팅방 생성은 **chat 서비스** (`meeting.created`)
- 해체된 모임은 **공개 목록에서 제외**
- 완료된 모임은 **기본 공개 목록에서 제외** (`status=COMPLETED`로만 조회)
- 모집완료는 목록 **하단**
- 강퇴(`KICKED`) 회원은 재신청 불가. 공개 목록 카드는 동일하게 보이며, 상세는 403
- 부방장 최대 1명 (meeting). 채팅 삭제/숨김/강퇴는 **chat**이 역할 이벤트 보고 강제
- 현재 참여 인원보다 작은 `maxMemberCount` 수정 불가
- 일정(`scheduleUuid`) 없으면 생성 불가
- 끌어올리기: 글로벌 트래블러·PLAN&WITH 마스터, **6시간** 간격
- 목록 정렬: `bump_at` 최신 → `created_at` 최신. **모집완료(`FULL`)는 하단**. 한 페이지 20개
- 목록 카드 스냅샷: 생성 시 schedule에서 `destination`, `start_date`, `end_date`를 meetings에 복사. 목록은 일정 서비스를 호출하지 않음
- 정원 가득 승인 시 자동 `FULL`. 인원 빠지면(나가기) 다시 `RECRUITING` 가능 (PDF: 모집 중단 = 다 찼음, 빠지면 입장 가능)
- 거절(`REJECTED`)·나가기(`LEFT`) 후 **재신청 가능**. 강퇴(`KICKED`)만 재신청·상세 진입 불가
- 강퇴 회원: **내 모임 목록에서 제외**. 공개 목록 카드는 보이되 상세는 403
- 승인 거절은 내 모임(승인대기)에 남기지 않음. 알림만 (notification 서비스)
- 해체 시 호스트 포함 전원 참여상태 `LEFT` 후 `DISBANDED`. 공개 목록에서 글 제거

---

## 요약

| 상태 | 이슈 | 내용 |
| --- | --- | --- |
| ✅ | #1 | 모임 서비스 골격·공통 응답·Gateway Trust |
| ✅ | #2 | 모임 생성 |
| ✅ | #3 | 모임 목록·상세 조회 |
| ✅ | #4 | 내 모임 조회 |
| ✅ | #5 | 모임 신청·승인·거절 |
| ✅ | #6 | 모임 수정·모집상태·끌어올리기 |
| ✅ | #7 | 모임 완료·해체 |
| ✅ | #8 | 구성원·부방장·강퇴·탈퇴 |
| ✅ | #11 | 채팅 서비스 연동 이벤트 발행 |
| ➡ chat | #9 #10 | 채팅 API — **이 레포 아님** (chat 서비스) |

작업 순서: `#1 → #2 → #3 → #4` 이후 `#5/#8` 과 `#6/#7` 병렬. 채팅 연동 `#11` 은 `#2`와 같이. 채팅 REST/SSE는 chat 레포.

---

## 예정 API

인증: `O` Gateway 로그인 / `X` 비회원 가능 / `optional` 로그인하면 내 상태 포함

채팅 REST/SSE (`/api/v1/chat-rooms/**`) 는 chat 서비스 이슈. 이 레포의 #9 #10 은 닫는다.

### 채팅 연동 이벤트 (이 레포는 Producer만, Kafka `EventEnvelope`)

| Issue | eventType | 언제 | payload | chat가 하는 일 |
| --- | --- | --- | --- | --- |
| #11 | `meeting.created` | 모임 생성 | meetingUuid, hostMemberUuid, title | `chat_rooms` 생성, 호스트 `APPROVED` |
| #11 | `meeting.completed` | 모임 완료 | meetingUuid | `chat_rooms.status=ENDED` |
| #11 | `meeting.disbanded` | 모임 해체 | meetingUuid | 방·멤버·Mongo 메시지 삭제 |
| #11 | `meeting.participation.changed` | 신청/승인/거절/탈퇴/강퇴 | meetingUuid, memberUuid, status | `chat_members.status` 동기화 |
| #11 | `meeting.updated` | 모임 수정 | meetingUuid, hostMemberUuid | 신청자·참여자 알림 (채팅 내용 제외) |
| #11 | `meeting.vice-host.changed` | 부방장 지정/해제/변경 | meetingUuid, viceHostMemberUuid | chat 관리 권한 + 등업 알림 |

Envelope: `eventId`, `eventType`, `occurredAt`, `aggregateId`(meetingUuid), `version`, `payload`. 토큰·비밀번호 없음.  
토픽: `planwith.meeting.{created|completed|disbanded|participation.changed|updated|vice-host.changed}`.  
로컬/테스트는 `MEETING_KAFKA_ENABLED=false`(로그 NoOp). 운영은 `true`. 발행 실패는 producer 재시도 후 로그만. DLT는 chat consumer.

상태 매핑 (PDF → API): `ing`→`RECRUITING`, `pull`(모집 중단/다 참)→`FULL`, `finish`→`COMPLETED`, `delete`→`DISBANDED`.  
참여: 대기=`PENDING`, 참여=`APPROVED`, 거절=`REJECTED`, 퇴장=`LEFT`, 강퇴=`KICKED`.

---

## 이벤트스토밍 대조 (2026-08-20 PDF)

모임 탭~강퇴/승인까지 커맨드는 **새 REST 없이** 기존 #2~#8로 커버한다. 빠진 것은 규칙·이벤트다.

| PDF 커맨드 | meeting API | 비고 |
| --- | --- | --- |
| 모임 탭 / 목록 (최신순) | `GET /meetings` #3 | 카드 20개씩. 목적지·기간은 생성 시 스냅샷 |
| 내 모임 (만든/참여/대기) | `GET /meetings/me` #4 | 거절·강퇴는 이 리스트에 없음 |
| 상세 (guest/host 버튼) | `GET /meetings/{uuid}` #3 | 모임 필드 + `scheduleUuid`. 여행 정보는 schedule |
| 생성 (+채팅방 생성) | `POST /meetings` #2 + 이벤트 #11 | 일정 코드만. 기간·비용은 body에 넣지 않음 |
| 수정 | `PATCH /meetings/{uuid}` #6 | `meeting.updated` 알림 |
| 끌어올리기 | `POST .../bump` #6 | 6시간, 특정 등급 |
| 모집 중단/재개 | `PATCH .../recruitment-status` #6 | 정원 가득 시 자동 pull |
| 완료 | `POST .../complete` #7 | 채팅 ENDED |
| 해체 | `POST .../disband` #7 | 글 삭제, 전원 퇴장, 채팅 삭제 |
| 신청/승인/거절 | applications #5 | 거절 후 재신청 가능 |
| 구성원 보기 | members #8 | 프로필은 member |
| 부방장 | vice-host #8 | `vice-host.changed` |
| 나가기/강퇴 | members me/uuid #8 | 강퇴는 상세 차단 |
| 채팅 입장·메시지 | chat 서비스 | meeting 아님 |
| 승인/거절/강퇴/수정 알림 | 이벤트 #11 → notification | meeting에 알림 REST 없음 |

---

## 완료된 API

| Issue | Method | Endpoint | 인증 | 설명 |
| --- | --- | --- | --- | --- |
| — | GET | `/api/planwith-fo-meeting/deploy-check` | X | 배포 확인 (스캐폴드) |
| #2 | POST | `/api/v1/meetings` | O | 모임 생성. body는 `scheduleUuid`+제목+소개+최대인원(+커버). 일정 상세 필드는 받지 않음 |
| #2 | POST | `/api/v1/meetings/{meetingUuid}/cover-image` | O host | 대표 이미지 stub `stub://meetings/{uuid}.ext` |
| #3 | GET | `/api/v1/meetings` | X | 카드: 사진·제목·인원·소개·목적지·기간 스냅샷·상태·방장(`hostMemberUuid`, `hostNickname`). `page`/`size`. 해체·완료 제외 |
| #3 | GET | `/api/v1/meetings/{meetingUuid}` | optional | 모임 상세(사진·제목·인원·소개·`scheduleUuid`). 강퇴 403, 해체 404. 여행 기간·비용 등은 schedule |
| #5 | POST | `/api/v1/meetings/{meetingUuid}/applications` | O | 신청. `PENDING`. 강퇴 재신청 불가. 거절/탈퇴 후 재신청 가능 |
| #5 | GET | `/api/v1/meetings/{meetingUuid}/applications` | O host | 승인 대기 목록 + 신청 메시지 |
| #5 | POST | `/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/approve` | O host | 승인 → `APPROVED`. 정원 차면 `FULL`. `participation.changed` |
| #5 | POST | `/api/v1/meetings/{meetingUuid}/applications/{memberUuid}/reject` | O host | 거절 → `REJECTED` |
| #5 | GET | `/api/v1/meetings/{meetingUuid}/participation` | O | 내 참여 상태 |
| #4 | GET | `/api/v1/meetings/me` | O | 내 모임. `scope=hosted\|joined\|pending`. 해체 제외, `FULL` 하단. hosted면 `canCreate=true` |
| #6 | PATCH | `/api/v1/meetings/{meetingUuid}` | O host | 소개·일정·최대인원 수정. 일정 스냅샷(장소·기간) 재복사. 현재 인원보다 작은 최대인원 불가. `meeting.updated` |
| #6 | PATCH | `/api/v1/meetings/{meetingUuid}/recruitment-status` | O host | `RECRUITING` ↔ `FULL`. 정원 가득이면 모집중 재개 불가 |
| #6 | POST | `/api/v1/meetings/{meetingUuid}/bump` | O host | 끌어올리기. 글로벌 트래블러·PLAN&WITH 마스터 stub, 6시간 간격 |
| #7 | POST | `/api/v1/meetings/{meetingUuid}/complete` | O host | 모임 완료(`COMPLETED`). 공개 목록 기본 제외. 상세·채팅 입장 유지. `meeting.completed` → chat `ENDED`(입력 불가) |
| #7 | POST | `/api/v1/meetings/{meetingUuid}/disband` | O host | 해체(`DISBANDED`). 전원 `LEFT`, 공개/상세 제외. `meeting.disbanded` → chat 방·메시지 삭제 |
| #8 | GET | `/api/v1/meetings/{meetingUuid}/members` | O participant | 참여 중(`APPROVED`) 구성원 목록. 역할·프로필 요약. OpenFeign 없이 member stub |
| #8 | GET | `/api/v1/meetings/{meetingUuid}/members/{memberUuid}` | O participant | 구성원 프로필 |
| #8 | PUT | `/api/v1/meetings/{meetingUuid}/vice-host` | O host | 부방장 지정/변경 (1명). 기존 부방장은 `MEMBER`로 강등. `vice-host.changed` |
| #8 | DELETE | `/api/v1/meetings/{meetingUuid}/vice-host` | O host | 부방장 해제. `vice-host.changed` |
| #8 | DELETE | `/api/v1/meetings/{meetingUuid}/members/me` | O joined | 탈퇴. 호스트 불가. `LEFT`, 정원 여유 시 `FULL`→`RECRUITING`. `participation.changed` |
| #8 | DELETE | `/api/v1/meetings/{meetingUuid}/members/{memberUuid}` | O host/vice | 강퇴 → `KICKED`. 재신청·상세 불가. `participation.changed` |

---

## 이 레포에서 하지 않음

요구사항 원문의 아래 항목은 **다른 FO 서비스** 이슈로 둔다.

- 마이페이지 / 프로필 / 비밀번호·이메일 / 탈퇴 / 로그아웃 → `planwith_fo_member` (#10 등)
- 팔로우 / 팔로워 / 회원 검색 → member (#27)
- 좋아요 / 내 스토리 → story
- 내 일정 / AI 일정 → schedule
- 멤버십 / 등급 조건·혜택·진행률 / Premium → grade
- 결제수단 / 결제내역 / 환불 → payment
- 토큰 보유·충전·차감 → token
- 앱 버전 / 오픈소스 / 문의하기 → 설정·FE
- 채팅방 CRUD / 메시지 / 공지 / SSE / 읽음 → **chat 서비스** (`chat_rooms`+`chat_members` MySQL, `chat_messages` Mongo)

---

## 갱신 규칙

이슈 구현 완료 시:

1. **요약** 상태 변경
2. API를 **완료** 표로 이동
3. README는 `docs/API.md` 링크만 유지
