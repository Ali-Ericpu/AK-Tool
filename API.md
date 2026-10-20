# HTTP API reference

This document describes the HTTP interface the AK Tool clients talk to. It is derived **from the
client code** (`shared/src/commonMain/kotlin/com/rainccup/aktool/core/network/`,
`…/core/model/` and `…/core/data/`), not from a server specification kept in this repository.
Where the client does not determine a behaviour, this document says so instead of guessing.

It deliberately covers only two things: **what each field must contain** and **the caveats worth
knowing before you implement a server against it**. The Kotlin models themselves are the source of
truth for their exact shape — they are not reproduced here.

- [1. Conventions](#1-conventions)
- [2. Authentication](#2-authentication)
- [3. Response envelope](#3-response-envelope)
- [4. Endpoints](#4-endpoints)
- [5. Field validation](#5-field-validation)
- [6. Static data](#6-static-data)
- [7. Error handling](#7-error-handling)
- [8. Caveats](#8-caveats)

---

## 1. Conventions

| Item | Rule |
|---|---|
| Paths | All `admin/...` paths are **relative**, with **no leading slash**, and are resolved against the configured base URL. |
| Bodies | JSON, `UTF-8`. |
| Header names | Lower-case: `uid`, `adminKey`. |
| JSON property names | Identical to the Kotlin property names — the client declares **no** `@SerialName` anywhere. |
| Base URL | `config.json` → `serverUri`. Blank falls back to `http://127.0.0.1`. A value with a port or path prefix is used verbatim, but only a bare host is covered by the project's tests. |

The `admin` prefix suggests a staff-facing surface; nothing in the client enforces that — it sends
whatever credentials it is configured with.

## 2. Authentication

Two headers carry the credentials, both read from `config.json` immediately before every call:

| Header | Config field | Notes |
|---|---|---|
| `uid` | `uid` | Account identifier. |
| `adminKey` | `adminKey` | Admin credential. |

Both default to the empty string — in which case the header is **still sent, empty**.

Three endpoints omit `uid` on purpose:

| Endpoint | Why |
|---|---|
| `POST admin/addFlushMessage` | carries the `uid` in the request body instead |
| `POST admin/account/register` | creates a new account, so there is no existing `uid` |
| `GET admin/syncValidCode` | not account-scoped as far as the client is concerned |

Static-data downloads ([§6](#6-static-data)) send **neither** header. There is no token exchange,
refresh, signing or expiry handling anywhere in the client.

## 3. Response envelope

Every `admin/...` response decodes into `ApiResult<T>`:

| Field | Type | Required | Meaning |
|---|---|---|---|
| `msg` | `String` | **yes** | Human-readable message. Used as the exception message on failure and as the fallback error when `data` is null on success. |
| `status` | `Int` | **yes** | `0` means success; every caller tests `status != 0` and throws. |
| `type` | `String` | **yes** | Decoded but **never read** by the client. The value vocabulary is a server concern. |
| `data` | `T?` | optional (`null` default) | Success payload. Callers null-check it rather than trusting `status` alone. |

Because `msg`, `status` and `type` are non-nullable with no defaults and the parser is not lenient,
**a response missing any of them fails to decode**. Extra properties are ignored
(`ignoreUnknownKeys = true`), so the server may add fields safely.

```json
{ "msg": "ok", "status": 0, "type": "OK", "data": null }
```

### Wire rules that both sides depend on

| Setting | Consequence |
|---|---|
| `encodeDefaults = true` | Request properties holding a default value **are** serialized. |
| `explicitNulls = false` | Request properties whose value is `null` are **omitted** from the body. |
| `ignoreUnknownKeys = true` | Unknown response properties are ignored. |

The second rule is what makes `admin/status/save` a **sparse patch**: only the fields the user
actually edited appear in the body.

## 4. Endpoints

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

“unmodelled” means the client types `data` as `JsonElement?` and never inspects its contents, so its
shape is undefined by this repository — rely on `status`.

`admin/character/sync` is keyed by the **instance id as a string**, matching `Character.instId`.

## 5. Field validation

### Read models

**`Status`** (`admin/status/sync`)

Only **five** properties are optional; **every other property is required**, so a server response
must include all of them.

| Optional property | Type | Notes |
|---|---|---|
| `flags` | `Map<String, Int>` | Free-form string→int map; the client stores and echoes it without interpreting the keys. |
| `mainStageProgress` | `String?` | — |
| `avatar` | `{ type: String?, id: String? }?` | — |
| `globalVoiceLan` | `String?` | — |
| `birthday` | `{ month: Int, day: Int }?` | Month and day only — **there is no year**. |

Rules that apply to the required fields:

- All counters are `Int`; timestamps are `Long`/`Int` with **units not established** by the client
  (a millisecond convention is not enforced anywhere).
- `birthday.month` must be `1..12` and `birthday.day` must be `1..daysInMonth(month)`, with
  **February treated as up to 29 days** (a year cannot be inferred). The client clamps dirty values
  — `2/30` becomes `2/29`, an invalid month becomes `1/1` — before showing or editing them.
- Several counters exist in two shapes (a modern set such as `gachaTicket` and older-style
  counterparts). Which one the server treats as authoritative is a server concern.
- The client ships a local placeholder `Status` for the first paint before the first sync; it is
  never sent to the server.

**`Character`** (`admin/character/sync` elements)

Required: `instId: Int`, `charId: String`, `favorPoint: Int`, `potentialRank: Int`,
`mainSkillLvl: Int`, `skin: String`, `level: Int`, `exp: Int`, `evolvePhase: Int`,
`defaultSkillIndex: Int`, `gainTime: Long`, `voiceLan: String`, `starMark: Int`.

Optional: `skills` (default empty), `currentEquip` (nullable), `equip` (default empty),
`currentTmpl` (nullable), `tmpl` (nullable).

**`name`, `profession` and `rarity` are `@Transient`.** They are never sent to or received from the
server: the client fills them in locally from `character_table.json` ([§6](#6-static-data)), taking
`rarity` from the table's rarity string (the part after the underscore, as an integer). A server
does not need to provide them.

| Nested type | Required fields | Notes |
|---|---|---|
| `Skill` | `skillId: String`, `unlock: Int`, `state: Int`, `specializeLevel: Int`, `completeUpgradeTime: Long` | — |
| `Equip` | `hide: Int`, `level: Int`, `locked: Int` | `locked == 1` means locked; `hide == 0` with a non-original equipment type means visible. |
| `TmplChar` | `skinId: String`, `defaultSkillIndex: Int`, `skills: List<Skill>`, `equip: Map<String, Equip>` | `currentEquip` optional/nullable. Used for per-template overrides. |

**`Item`** — `id: String`, `type: String`, `count: Int`, all required.

### Write models

All properties of every write model are **required** except `SaveStatusRequest`'s, which are all
optional. None declares `@SerialName`, so property names are the wire names.

| Model | Fields | Validation / notes |
|---|---|---|
| `SaveCharRequest` | `charInstId: Int`, `char: Character` | Send the **full** character object; the `@Transient` trio above is not serialized. |
| `GainItemRequest` | `items: List<Item>` | The client grants a character with `type = "CHAR"` and `count = 1`. |
| `SaveStatusRequest` | all optional (see below) | A sparse patch: null fields are omitted. |
| `UnlockAllCharRequest` | `favorPoint`, `potentialRank`, `specializeLevel`, `mainSkillLvl`, `evolvePhase`, `level`, `equipLevel` (all `Int`), `enableRogueChar: Boolean` | Every field required. The values come from the client's own sliders — no range is enforced by the model. |
| `AddFlushMessageRequest` | `uid: String`, `message: String` | Sent **without** a `uid` header. |
| `ResetActivityRequest` | `type: String`, `id: String` | Both free-form strings supplied by the UI. |
| `RegisterAccountRequest` | `account: String`, `password: String` | **Client-side validation**: the account must be non-blank and at most **15** characters; the password must be non-blank. |

`SaveStatusRequest` accepts: `nickName`, `nickNumber`, `level`, `diamond`, `diamondShard`, `ap`,
`gold`, `hggShard`, `lggShard`, `gachaTkt`, `tenGachaTkt`, `classicGachaTkt`, `tenClassicGachaTkt`,
`classicShard`, `tryTkt`, `recTkt`, `fniTkt` (all `Int?`/`String?`) and
`birthday: { month, day }?`.

### Read/write field-name divergences

The read and write models **do not share field names** for most counters, and the client contains
no mapping between them. Compare carefully before implementing `admin/status/save`:

| Concept | `Status` (read) | `SaveStatusRequest` (write) |
|---|---|---|
| Premium currency | `androidDiamond`, `iosDiamond` | `diamond` |
| Premium currency shards | `diamondShard` | `diamondShard` |
| Energy / stamina | `ap` | `ap` |
| Soft currency | `gold` | `gold` |
| Shard counters | `hggShard`, `lggShard` | `hggShard`, `lggShard` |
| Standard ticket pair | `gachaTicket`, `tenGachaTicket` | `gachaTkt`, `tenGachaTkt` |
| Classic ticket pair | `classicGachaTicket`, `classicTenGachaTicket` | `classicGachaTkt`, `tenClassicGachaTkt` |
| Classic shards | `classicShard` | `classicShard` |
| Practice ticket | `practiceTicket` | `tryTkt` |
| Recruitment licence | `recruitLicense` | `recTkt` |
| Instant-finish ticket | `instantFinishTicket` | `fniTkt` |
| Nickname / number / level | `nickName`, `nickNumber`, `level` | same names |
| Birthday | `birthday` | `birthday` (same shape; omitted when `null`) |

The write model also has no counterpart for `exp`, `socialPoint`, `progress`, `buyApRemainTimes`,
`apLimitUpFlag`, `maxAp` or the avatar/skin/timestamp fields — those appear to be server-managed.

## 6. Static data

Not part of the `admin` API and **not** wrapped in the envelope. The client downloads four JSON
documents as raw text:

```
GET <serverUri>/assetbundle/excel/character_table.json
GET <serverUri>/assetbundle/excel/favor_table.json
GET <serverUri>/assetbundle/excel/uniequip_table.json
GET <serverUri>/assetbundle/excel/skin_table.json
```

- `serverUri` is used with any trailing `/` trimmed, then `/assetbundle/excel/<name>` is appended.
- **No `uid`/`adminKey` headers** are sent.
- Each response must be a JSON document. The client parses them into untyped maps, not into declared
  models, so their internal shape is only constrained by the few fields it reads: elite phases and
  their max level, favour frames and their percent values, equipment type icons, skin ids and names.
- The files are cached at `<dataDir>/excel/…` and **all four must be present and non-empty** before
  character data can be enriched; downloading is triggered from the Settings screen and refuses to
  run with a blank `serverUri`.

## 7. Error handling

| Situation | Client behaviour |
|---|---|
| Non-2xx HTTP status | Ktor throws **before** the body is parsed; the message is transport-level. |
| 2xx with `status != 0` | Callers throw `RuntimeException(msg)`, surfaced through the snackbar. |
| 2xx with `status == 0` but `data == null` | On endpoints where `data` is required, callers also fail, using `msg` as the message. |
| Body missing `msg`/`status`/`type` | Decoding fails; this surfaces as a serialization error, not a clean API error. |
| Timeout | **No timeout is configured**, so a stalled connection is bounded only by engine defaults. |

The repository layer performs **no** envelope checking — it forwards `ApiResult` unchanged. All
`status`/`msg`/`data` interpretation happens in use cases, which is why this document describes the
envelope once rather than per-call error codes.

## 8. Caveats

1. **`data` for write endpoints is unmodelled.** Ten endpoints return `JsonElement?`; the client
   never inspects it, so its shape is undefined here. Treat it as informational and rely on `status`.
2. **`type` semantics are unknown.** Only `"OK"` appears in the project's fixtures and no client
   code reads the field.
3. **`status` values beyond 0/1 are unknown.** The client only tests `!= 0`.
4. **No retries and no timeout.** Failures surface to the user as they happen.
5. **Base URLs with a port or path prefix are untested** by the project's tests, which cover only a
   bare host and the empty string.
6. **Timestamp units are not established.** The models use `Long`/`Int` with no conversion anywhere
   in the client.
7. **Credentials travel in headers, in cleartext, when the base URL is `http://`.** The Android
   manifest permits cleartext traffic; prefer TLS and a trusted network.
8. **`downloadText` is unauthenticated and absolute.** If you point it at a third-party host, that
   host receives the request with no credentials — and none of your data.

If you change the client, update this document alongside
`shared/src/commonTest/kotlin/com/rainccup/aktool/core/network/ApiClientTest.kt`, which pins several
of the transport behaviours described above (relative path resolution, header presence and
null-omission in request bodies).
