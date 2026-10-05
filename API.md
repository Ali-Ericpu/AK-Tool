# HTTP API reference

This document describes the HTTP interface used by the AK Tool clients. It is derived **from
the client code** (`shared/src/commonMain/kotlin/com/rainccup/aktool/core/network/`,
`…/core/model/` and `…/core/data/`) at revision `13fa17d`, not from a server specification that
lives in this repository. Where the client does not determine a behaviour, this document says
so explicitly instead of guessing.

- [1. Scope and conventions](#1-scope-and-conventions)
- [2. Transport](#2-transport)
- [3. Authentication](#3-authentication)
- [4. Response envelope](#4-response-envelope)
- [5. Endpoint index](#5-endpoint-index)
- [6. Endpoint details](#6-endpoint-details)
- [7. Response models](#7-response-models)
- [8. Request models](#8-request-models)
- [9. Read/write field-name divergences](#9-readwrite-field-name-divergences)
- [10. Static data endpoints](#10-static-data-endpoints)
- [11. Error handling](#11-error-handling)
- [12. Caveats and open questions](#12-caveats-and-open-questions)

---

## 1. Scope and conventions

- All `admin/...` paths are **relative** and are resolved against the configured base URL
  (see [§2](#2-transport)). They have no leading slash.
- All request and response bodies are JSON, `UTF-8`.
- Header names are lower-case: `uid`, `adminKey`.
- JSON property names are identical to the Kotlin property names — the client declares **no**
  `@SerialName` overrides anywhere in `shared/src`.
- The `admin` prefix suggests a staff-facing / administrative surface. Nothing in the client
  enforces that; it simply sends the credentials it is configured with.

Throughout this document the base URL is written as `https://your-server.example`.

---

## 2. Transport

### Base URL

| Item | Value |
|---|---|
| Code default | `http://127.0.0.1` (`NetworkConfig.DEFAULT_BASE_URL`) |
| Effective value | `NetworkConfig.baseUrl`, falling back to the default whenever it is blank |
| Stored in | `config.json` field `serverUri` |
| Edited from | the Settings screen (`ConfigStore.save`) |

Paths are joined as `baseUrl` + `path`. The default carries **no port and no path**; a
`serverUri` that includes a port or a path prefix is accepted verbatim by the client, but only
a bare host is covered by the project's tests.

Changing `serverUri` recreates the cached HTTP client (`HttpClientProvider.recreate`), so the
next request uses the new host.

### Client construction

One factory builds the client (`core/network/HttpClientFactory.kt`):

```kotlin
fun createHttpClient(
    config: NetworkConfig,
    engine: HttpClientEngine? = null,
    expectSuccess: Boolean = true,
): HttpClient
```

- `expectSuccess = true` in production: a non-2xx HTTP status raises an exception **before** any
  envelope parsing (see [§11](#11-error-handling)).
- `engine` is `null` in production, so Ktor picks the engine from the classpath. Engine choice is
  a per-source-set dependency, not a source-level class reference:

  | Target | Declared dependency | Engine |
  |---|---|---|
  | Android | `io.ktor:ktor-client-okhttp` | OkHttp |
  | Desktop (JVM) | `io.ktor:ktor-client-cio` | CIO |
  | Tests | `io.ktor:ktor-client-mock` | `MockEngine`, injected explicitly |

- `ContentNegotiation` is installed with a `Json` instance configured as:

  ```kotlin
  Json {
      ignoreUnknownKeys = true
      encodeDefaults = true
      explicitNulls = false
      prettyPrint = false
  }
  ```

  Two of these are wire-visible and worth remembering:

  | Setting | Consequence |
  |---|---|
  | `encodeDefaults = true` | Request properties that hold a default value are still serialized. |
  | `explicitNulls = false` | Request properties whose value is `null` are **omitted** from the body. |
  | `ignoreUnknownKeys = true` | Extra properties in a response are ignored, so the server may add fields safely. |

- `Logging` is installed at `LogLevel.INFO`, writing through Kermit. Request/response bodies of
  authenticated calls can therefore appear in logs.
- `defaultRequest` sets the base URL and `Content-Type: application/json` for every request.
- **No timeouts are configured.** There is no `HttpTimeout` plugin, and no connect/socket/
  request timeout values in source. Effective limits are whatever the engines default to.

---

## 3. Authentication

Two headers carry credentials. Both come from `NetworkConfig`, which is refreshed from
`config.json` immediately before every repository call (`AdminRepository.syncCredentials()`):

| Header | Config field | Notes |
|---|---|---|
| `uid` | `uid` | Account identifier. |
| `adminKey` | `adminKey` | Admin credential. |

Both default to the empty string, in which case the header is still sent, empty.

**Endpoints that send only `adminKey`** (no `uid`):

| Endpoint | Why it can omit `uid` |
|---|---|
| `POST admin/addFlushMessage` | carries a `uid` in the request **body** instead |
| `POST admin/account/register` | creates a new account, so there is no existing `uid` |
| `GET admin/syncValidCode` | not account-scoped as far as the client is concerned |

**`GET <absolute url>` (`downloadText`)** sends no `uid`/`adminKey` headers at all.

There is no token exchange, refresh, signing or expiry handling anywhere in the client.

---

## 4. Response envelope

Every `admin/...` call except `downloadText` decodes into:

```kotlin
@Serializable
data class ApiResult<T>(
    val msg: String,
    val status: Int,
    val type: String,
    val data: T? = null,
)
```

| Field | Type | Required | Meaning in client code |
|---|---|---|---|
| `msg` | `String` | **yes** | Human-readable message. Used as the exception message on failure, and as a fallback error when `data` is null on success. |
| `status` | `Int` | **yes** | `0` means success. Every caller tests `status != 0` and throws. |
| `type` | `String` | **yes** | Decoded but **never read** by the client. Only the literal `"OK"` appears in the project's fixtures; the value vocabulary is a server concern. |
| `data` | `T?` | no (`null` default) | Success payload. Nullable, and callers null-check it rather than trusting `status` alone. |

Because `msg`, `status` and `type` are non-nullable with no defaults and the JSON parser is not
lenient, a response missing any of them fails to decode.

Minimal success response:

```json
{ "msg": "ok", "status": 0, "type": "OK", "data": null }
```

Failure response (client raises `RuntimeException(msg)`):

```json
{ "msg": "account not found", "status": 1, "type": "ERROR", "data": null }
```

---

## 5. Endpoint index

| # | Method | Path | `uid` | `adminKey` | Request body | `data` on success |
|---|---|---|---|---|---|---|
| 1 | GET | `admin/character/sync` | ✔ | ✔ | — | object map: instance id → character |
| 2 | POST | `admin/character/save` | ✔ | ✔ | `SaveCharRequest` | unmodelled |
| 3 | POST | `admin/gainItem` | ✔ | ✔ | `GainItemRequest` | unmodelled |
| 4 | GET | `admin/status/sync` | ✔ | ✔ | — | `Status` |
| 5 | POST | `admin/status/save` | ✔ | ✔ | `SaveStatusRequest` | unmodelled |
| 6 | POST | `admin/unlockAllChar` | ✔ | ✔ | `UnlockAllCharRequest` | unmodelled |
| 7 | POST | `admin/unlockAllStages` | ✔ | ✔ | none | unmodelled |
| 8 | POST | `admin/unlockAllFlags` | ✔ | ✔ | none | unmodelled |
| 9 | POST | `admin/addFlushMessage` | ✗ | ✔ | `AddFlushMessageRequest` | unmodelled |
| 10 | POST | `admin/resetActivity` | ✔ | ✔ | `ResetActivityRequest` | unmodelled |
| 11 | POST | `admin/account/register` | ✗ | ✔ | `RegisterAccountRequest` | unmodelled |
| 12 | POST | `admin/account/reset` | ✔ | ✔ | none | unmodelled |
| 13 | GET | `admin/syncValidCode` | ✗ | ✔ | — | string map |
| 14 | POST | `admin/rlv2/reset` | ✔ | ✔ | none | unmodelled |
| 15 | POST | `admin/account/queryAccountByUID` | ✔ | ✔ | none | string map |
| 16 | GET | *(caller-supplied absolute URL)* | ✗ | ✗ | — | raw text, **no envelope** |

"unmodelled" means the client types `data` as `JsonElement?` and never inspects its contents.

---

## 6. Endpoint details

### 6.1 `GET admin/character/sync`

Returns every character on the account, keyed by instance id.

- **Response**: `ApiResult<Map<String, Character>>` — the key is the instance id as a string,
  matching `Character.instId`.
- The client treats a null `data` as an error and surfaces `msg`.

```bash
curl -s "https://your-server.example/admin/character/sync" \
  -H 'uid: 100000001' -H 'adminKey: <admin key>'
```

```json
{
  "msg": "ok",
  "status": 0,
  "type": "OK",
  "data": {
    "1001": {
      "instId": 1001,
      "charId": "char_000_example",
      "favorPoint": 55,
      "potentialRank": 1,
      "mainSkillLvl": 7,
      "skin": "char_000_example#1",
      "level": 60,
      "exp": 0,
      "evolvePhase": 1,
      "defaultSkillIndex": 0,
      "gainTime": 1600000000,
      "skills": [
        { "skillId": "skl_000_exa_1", "unlock": 0, "state": 0, "specializeLevel": 1, "completeUpgradeTime": -1 }
      ],
      "voiceLan": "CN",
      "currentEquip": null,
      "equip": { "uniequip_000_exa": { "hide": 1, "level": 1, "locked": 0 } },
      "starMark": 0,
      "currentTmpl": null,
      "tmpl": null
    }
  }
}
```

`name`, `profession` and `rarity` are **client-side enrichment** — see
[§7.2](#72-character).

### 6.2 `POST admin/character/save`

Persists one character.

- **Body**: `SaveCharRequest` — `{ "charInstId": <int>, "char": <Character> }`. The full
  character object is sent; the `@Transient` fields (`name`, `profession`, `rarity`) are not
  serialized.
- **Response**: `ApiResult<JsonElement?>`; only `status`/`msg` are meaningful to the client.

```bash
curl -s -X POST "https://your-server.example/admin/character/save" \
  -H 'uid: 100000001' -H 'adminKey: <admin key>' \
  -H 'Content-Type: application/json' \
  -d '{"charInstId":1001,"char":{"instId":1001,"charId":"char_000_example","favorPoint":60,"potentialRank":1,"mainSkillLvl":7,"skin":"char_000_example#1","level":61,"exp":0,"evolvePhase":1,"defaultSkillIndex":0,"gainTime":1600000000,"skills":[],"voiceLan":"CN","currentEquip":null,"equip":{},"starMark":0,"currentTmpl":null,"tmpl":null}}'
```

### 6.3 `POST admin/gainItem`

Grants items to the account.

- **Body**: `GainItemRequest` — `{ "items": [ { "id": …, "type": …, "count": … } ] }`.
- The client uses this to hand out a single character by id, with `type` `"CHAR"` and
  `count` `1`.

```bash
curl -s -X POST "https://your-server.example/admin/gainItem" \
  -H 'uid: 100000001' -H 'adminKey: <admin key>' \
  -H 'Content-Type: application/json' \
  -d '{"items":[{"id":"char_000_example","type":"CHAR","count":1}]}'
```

### 6.4 `GET admin/status/sync`

Returns the account-level status document.

- **Response**: `ApiResult<Status>` — full model in [§7.1](#71-status).

```bash
curl -s "https://your-server.example/admin/status/sync" \
  -H 'uid: 100000001' -H 'adminKey: <admin key>'
```

### 6.5 `POST admin/status/save`

Updates account-level values. Every property is optional; the client sends only the fields the
user edited, because null-valued properties are omitted from the body.

- **Body**: `SaveStatusRequest` — see [§8](#8-request-models).
- **Response**: `ApiResult<JsonElement?>`.

```bash
# only `level` is transmitted here; all other properties are null and therefore omitted
curl -s -X POST "https://your-server.example/admin/status/save" \
  -H 'uid: 100000001' -H 'adminKey: <admin key>' \
  -H 'Content-Type: application/json' \
  -d '{"level":120}'
```

> Field naming differs from `Status`; read [§9](#9-readwrite-field-name-divergences) before
> wiring this endpoint up.

### 6.6 `POST admin/unlockAllChar`

Unlocks everything, applying the same progression values to every character.

- **Body**: `UnlockAllCharRequest` — see [§8](#8-request-models).
- **Response**: `ApiResult<JsonElement?>`.

```bash
curl -s -X POST "https://your-server.example/admin/unlockAllChar" \
  -H 'uid: 100000001' -H 'adminKey: <admin key>' \
  -H 'Content-Type: application/json' \
  -d '{"favorPoint":100,"potentialRank":5,"specializeLevel":3,"mainSkillLvl":7,"evolvePhase":2,"level":90,"equipLevel":3,"enableRogueChar":true}'
```

### 6.7 `POST admin/unlockAllStages`

- **Body**: none.
- **Response**: `ApiResult<JsonElement?>`.

### 6.8 `POST admin/unlockAllFlags`

- **Body**: none.
- **Response**: `ApiResult<JsonElement?>`.

### 6.9 `POST admin/addFlushMessage`

Queues a message for delivery to the account. **Not sent with a `uid` header** — the account is
named in the body.

- **Body**: `AddFlushMessageRequest` — `{ "uid": …, "message": … }`.
- **Response**: `ApiResult<JsonElement?>`.

```bash
curl -s -X POST "https://your-server.example/admin/addFlushMessage" \
  -H 'adminKey: <admin key>' \
  -H 'Content-Type: application/json' \
  -d '{"uid":"100000001","message":"hello"}'
```

### 6.10 `POST admin/resetActivity`

Resets a named activity.

- **Body**: `ResetActivityRequest` — `{ "type": …, "id": … }`.
- **Response**: `ApiResult<JsonElement?>`.

### 6.11 `POST admin/account/register`

Creates an account. **Not sent with a `uid` header.**

- **Body**: `RegisterAccountRequest` — `{ "account": …, "password": … }`.
- **Response**: `ApiResult<JsonElement?>`.
- The client validates the account name before sending: it must be non-blank and at most 15
  characters; the password must be non-blank.

```bash
curl -s -X POST "https://your-server.example/admin/account/register" \
  -H 'adminKey: <admin key>' \
  -H 'Content-Type: application/json' \
  -d '{"account":"example01","password":"<password>"}'
```

### 6.12 `POST admin/account/reset`

- **Body**: none.
- **Response**: `ApiResult<JsonElement?>`.
- Resets the account identified by the `uid` header.

### 6.13 `GET admin/syncValidCode`

- **Sent with `adminKey` only.**
- **Response**: `ApiResult<Map<String, String>>` — a string-to-string map. The client does not
  interpret the keys or values.

### 6.14 `POST admin/rlv2/reset`

- **Body**: none.
- **Response**: `ApiResult<JsonElement?>`.

### 6.15 `POST admin/account/queryAccountByUID`

- **Body**: none; the account is taken from the `uid` header.
- **Response**: `ApiResult<Map<String, String>>`.

### 6.16 `GET <absolute URL>` (raw download)

Used only for static data files ([§10](#10-static-data-endpoints)). It is **not** an `admin`
endpoint:

- the URL is passed in full and used verbatim (the base URL is not prepended),
- no `uid`/`adminKey` headers are sent,
- the response body is returned as **raw text** with no envelope decoding.

---

## 7. Response models

### 7.1 `Status`

```kotlin
@Serializable
data class Status(
    val nickName: String,
    val nickNumber: String,
    val level: Int,
    val exp: Int,
    val socialPoint: Int,
    val gachaTicket: Int,
    val tenGachaTicket: Int,
    val instantFinishTicket: Int,
    val hggShard: Int,
    val lggShard: Int,
    val recruitLicense: Int,
    val progress: Int,
    val buyApRemainTimes: Int,
    val apLimitUpFlag: Int,
    val uid: String,
    val flags: MutableMap<String, Int> = mutableMapOf(),
    val ap: Int,
    val maxAp: Int,
    val androidDiamond: Int,
    val iosDiamond: Int,
    val diamondShard: Int,
    val gold: Int,
    val practiceTicket: Int,
    val lastRefreshTs: Long,
    val lastApAddTime: Long,
    val mainStageProgress: String? = null,
    val registerTs: Long,
    val lastOnlineTs: Long,
    val serverName: String,
    val avatarId: String,
    val resume: String,
    val friendNumLimit: Int,
    val monthlySubscriptionStartTime: Int,
    val monthlySubscriptionEndTime: Int,
    val secretary: String,
    val secretarySkinId: String,
    val tipMonthlyCardExpireTs: Int,
    val avatar: Avatar? = null,
    val globalVoiceLan: String? = null,
    val classicShard: Int,
    val classicGachaTicket: Int,
    val classicTenGachaTicket: Int,
)

@Serializable
data class Avatar(
    val type: String? = null,
    val id: String? = null,
)
```

Notes:

- Only `flags`, `mainStageProgress`, `avatar` and `globalVoiceLan` are optional; **every other
  property is required**, so a server response must include them.
- `flags` is a free-form string→int map; the client stores and echoes it without interpreting
  the keys.
- Timestamps (`lastRefreshTs`, `lastApAddTime`, `registerTs`, `lastOnlineTs`,
  `tipMonthlyCardExpireTs`, `monthlySubscription*`) are numeric; **units are not established by
  the client** (the millisecond convention is not enforced anywhere).
- Several counters exist in two shapes — a modern set (`gachaTicket`, `classicGachaTicket`, …)
  and older-style counterparts. Which of them the server treats as authoritative is a server
  concern.
- The client ships a local placeholder `Status` (for first paint before the first sync) whose
  values are unrelated to your account; it is never sent to the server.

### 7.2 `Character`

```kotlin
@Serializable
data class Character(
    val instId: Int,
    val charId: String,
    @Transient val name: String? = null,
    @Transient val profession: String? = null,
    @Transient val rarity: Int? = null,
    val favorPoint: Int,
    val potentialRank: Int,
    val mainSkillLvl: Int,
    val skin: String,
    val level: Int,
    val exp: Int,
    val evolvePhase: Int,
    val defaultSkillIndex: Int,
    val gainTime: Long,
    val skills: List<Skill> = emptyList(),
    val voiceLan: String,
    val currentEquip: String? = null,
    val equip: Map<String, Equip> = emptyMap(),
    val starMark: Int,
    val currentTmpl: String? = null,
    val tmpl: Map<String, TmplChar>? = null,
) : Comparable<Character>
```

Important detail: **`name`, `profession` and `rarity` are `@Transient`.** They are never sent to
or received from the server. The client fills them in locally, after `character/sync`, by
looking the character up in the downloaded `character_table.json`
([§10](#10-static-data-endpoints)); `rarity` is derived from that table's rarity string by
taking the part after the underscore as an integer.

Nested types:

```kotlin
@Serializable
data class Skill(
    val skillId: String,
    val unlock: Int,
    val state: Int,
    val specializeLevel: Int,
    val completeUpgradeTime: Long,
)

@Serializable
data class Equip(
    val hide: Int,
    val level: Int,
    val locked: Int,
)

@Serializable
data class TmplChar(
    val skinId: String,
    val defaultSkillIndex: Int,
    val skills: List<Skill>,
    val currentEquip: String? = null,
    val equip: Map<String, Equip>,
)
```

- `Equip` in the client: `locked == 1` means locked; `hide == 0` with a non-original equipment
  type means the equipment is visible.
- `tmpl` carries per-template overrides and may be `null`.

### 7.3 `Item`

```kotlin
@Serializable
data class Item(
    val id: String,
    val type: String,
    val count: Int,
)
```

Used as the element type of `GainItemRequest.items`. The client sends `type` `"CHAR"` when
granting a character.

---

## 8. Request models

None of these declare `@SerialName`; property names are the wire names.

```kotlin
@Serializable
data class SaveCharRequest(
    val charInstId: Int,
    val char: Character,
)

@Serializable
data class GainItemRequest(val items: List<Item>)

@Serializable
data class SaveStatusRequest(
    val nickName: String? = null,
    val nickNumber: String? = null,
    val level: Int? = null,
    val diamond: Int? = null,
    val diamondShard: Int? = null,
    val ap: Int? = null,
    val gold: Int? = null,
    val hggShard: Int? = null,
    val lggShard: Int? = null,
    val gachaTkt: Int? = null,
    val tenGachaTkt: Int? = null,
    val classicGachaTkt: Int? = null,
    val tenClassicGachaTkt: Int? = null,
    val classicShard: Int? = null,
    val tryTkt: Int? = null,
    val recTkt: Int? = null,
    val fniTkt: Int? = null,
)

@Serializable
data class UnlockAllCharRequest(
    val favorPoint: Int,
    val potentialRank: Int,
    val specializeLevel: Int,
    val mainSkillLvl: Int,
    val evolvePhase: Int,
    val level: Int,
    val equipLevel: Int,
    val enableRogueChar: Boolean,
)

@Serializable
data class AddFlushMessageRequest(
    val uid: String,
    val message: String,
)

@Serializable
data class ResetActivityRequest(
    val type: String,
    val id: String,
)

@Serializable
data class RegisterAccountRequest(
    val account: String,
    val password: String,
)
```

`SaveStatusRequest` is the only model whose properties are all optional; because null values
are omitted on the wire, a request is a sparse patch. All other request models require every
property.

---

## 9. Read/write field-name divergences

The read model (`Status`) and the write model (`SaveStatusRequest`) **do not share field
names** for most counters, and the client contains no mapping between them. Compare before
implementing against them:

| Concept | `Status` (read) | `SaveStatusRequest` (write) |
|---|---|---|
| Premium currency | `androidDiamond`, `iosDiamond` | `diamond` |
| Premium currency shards | `diamondShard` | `diamondShard` |
| Energy/stamina | `ap` | `ap` |
| Soft currency | `gold` | `gold` |
| Shard counters | `hggShard`, `lggShard` | `hggShard`, `lggShard` |
| Standard ticket pair | `gachaTicket`, `tenGachaTicket` | `gachaTkt`, `tenGachaTkt` |
| Classic ticket pair | `classicGachaTicket`, `classicTenGachaTicket` | `classicGachaTkt`, `tenClassicGachaTkt` |
| Classic shards | `classicShard` | `classicShard` |
| Practice ticket | `practiceTicket` | `tryTkt` |
| Recruitment licence | `recruitLicense` | `recTkt` |
| Instant-finish ticket | `instantFinishTicket` | `fniTkt` |
| Nickname / number / level | `nickName`, `nickNumber`, `level` | same names |

The write model also has no counterpart for `exp`, `socialPoint`, `progress`,
`buyApRemainTimes`, `apLimitUpFlag`, `maxAp` or the avatar/skin/timestamp fields — those are
apparently server-managed.

---

## 10. Static data endpoints

Not part of the `admin` API and not wrapped in the envelope. The client downloads three JSON
documents as raw text:

```
GET <serverUri>/assetbundle/excel/character_table.json
GET <serverUri>/assetbundle/excel/favor_table.json
GET <serverUri>/assetbundle/excel/uniequip_table.json
```

- `serverUri` is used with any trailing `/` trimmed, then `/assetbundle/excel/<name>` is
  appended.
- **No `uid`/`adminKey` headers** are sent.
- The response must be a JSON document; the client parses it into untyped maps rather than into
  declared models.
- Files are cached on disk (`<dataDir>/excel/…`) and required to be present before character
  data can be enriched. Downloading is triggered from the Settings screen and refuses to run
  with a blank `serverUri`.

---

## 11. Error handling

Two distinct failure channels, and they behave differently:

| Situation | Client behaviour |
|---|---|
| Non-2xx HTTP status | Ktor throws (`expectSuccess = true`) **before** the body is parsed. The message is transport-level. |
| 2xx with `status != 0` | Callers throw `RuntimeException(result.msg)`, which the UI surfaces through the message bus/snackbar. |
| 2xx with `status == 0` but `data == null` | On endpoints where `data` is required, callers also fail, using `msg` as the message. |
| Body missing `msg`/`status`/`type` | Decoding fails; this surfaces as a serialization error, not a clean API error. |
| Timeout | No timeout is configured, so a stalled connection is bounded only by engine defaults. |

The repository layer (`AdminRepository`) performs **no** envelope checking — it forwards
`ApiResult` unchanged. All `status`/`msg`/`data` interpretation happens in use cases and view
models, which is why every endpoint documents the envelope rather than per-call error codes.

---

## 12. Caveats and open questions

1. **`data` for write endpoints is unmodelled.** Ten endpoints return `ApiResult<JsonElement?>`;
   the client never inspects that payload, so its shape is undefined by this repository. Treat
   it as informational and rely on `status`.
2. **`type` semantics are unknown.** Only `"OK"` appears in fixtures, and no client code reads
   the field.
3. **`status` values beyond 0/1 are unknown.** The client only tests `!= 0`.
4. **Timeout and retry policy are not defined here.** No retries exist; failures surface to the
   user.
5. **Base URLs with a port or path prefix are untested** by the project's own tests, which cover
   only a bare host and the empty string.
6. **Timestamp units are not established.** The models use `Long`/`Int` without any conversion
   in the client.
7. **Credentials travel in headers, in cleartext, when the base URL is `http://`.** The Android
   manifest permits cleartext traffic. Prefer TLS and a trusted network.
8. **`downloadText` is unauthenticated and absolute.** If you point it at a third-party host,
   that host receives your request with no credentials — but also with none of your data.

If you change the client, update this document alongside
`shared/src/commonTest/kotlin/com/rainccup/aktool/core/network/ApiClientTest.kt`, which pins
several of the transport behaviours described above (relative path resolution, header
presence, and null-omission in request bodies).
