# Marksman Lane Insights And Suggestions Spec

## Goal And Context

Define the first-release marksman-lane analysis feature that turns one saved
match into:

- detailed role-specific metrics
- useful, deterministic suggestions
- honest limitation messaging when current saved data is not sufficient

This spec follows
`docs/plans/2026-04-16-marksman-lane-insights-and-suggestions.md`.

The first release is intentionally scoped to one saved match in the existing
record-detail flow. It does not redefine the aggregate dashboard and it does
not add hero-specific coaching yet.

## Concrete Examples

### Example 1: Eligible Marksman-Lane Match With Strong Saved Coverage

Input:
- user opens a saved match detail
- `lane = 发育路`
- the saved match has values for `result`, `score`, `kda`, `damageDealt`,
  `damageShare`, `damageTaken`, `damageTakenShare`, `totalGold`, `goldShare`,
  `participationRate`, `goldFromFarming`, `lastHits`, and
  `damageDealtToOpponents`

Expected behavior:
- the detail screen shows a marksman-lane insights section
- the insights section shows the approved detailed metric groups
- the screen shows a bounded suggestion set derived from the saved match
- the existing raw grouped detail remains visible

### Example 2: Eligible Match With Partial Optional Inputs

Input:
- user opens a saved match detail
- `lane = 发育路`
- required saved fields are present, but optional fields such as
  `goldFromFarming`, `lastHits`, or `controlDuration` are empty

Expected behavior:
- the insights section still appears because the match is marksman-lane
  eligible
- metric groups with enough data remain visible
- metrics or suggestions that depend on missing optional data remain partial or
  unavailable rather than fabricated
- the screen explains insufficiency in a user-facing way instead of looking
  broken

### Example 3: Non-Marksman Lane Match

Input:
- user opens a saved match detail
- `lane = 中路`

Expected behavior:
- the detail screen does not pretend to analyze the match as a marksman-lane
  match
- the marksman section shows an explicit “not available for this lane” style
  message
- the existing raw grouped detail remains visible

### Example 4: Lane Missing Or Unresolved

Input:
- user opens a saved match detail
- `lane` is empty or unavailable in the saved record

Expected behavior:
- the screen does not guess whether the match was marksman-lane
- the marksman section shows an explicit “not enough saved data to determine
  marksman-lane analysis” style message
- the existing raw grouped detail remains visible

### Example 5: Existing Aggregate Dashboard

Input:
- user opens the dashboard after this feature ships

Expected behavior:
- the existing aggregate dashboard behavior remains unchanged in this first
  feature release
- marksman-lane insights are surfaced in per-match detail first, not silently
  mixed into the current dashboard contract

## Inputs And Outputs

Inputs:
- one locally saved match record
- only the saved fields already linked to that record

Outputs:
- a bounded marksman-lane insights section for the saved-record detail flow
- explicit eligible / unavailable / insufficient analysis states
- a bounded per-match metric set
- a bounded per-match suggestion set

## Requirements

- `R1` The first release of this feature MUST surface marksman-lane insights in
  the saved-record detail flow for one saved match at a time.

- `R2` A match MUST be treated as marksman-lane eligible only when the saved
  local record explicitly identifies the lane as `发育路`.

- `R3` If the saved lane is present and is not `发育路`, the system MUST show an
  explicit marksman-analysis unavailable state for that match instead of
  silently hiding the feature or attempting role analysis anyway.

- `R4` If the saved lane is missing or unresolved, the system MUST show an
  explicit insufficient-data state for marksman-lane analysis instead of
  guessing the role.

- `R5` Marksman-lane analysis MUST use only locally saved data from the current
  match. It MUST NOT call a server, cloud service, or external rule engine at
  runtime.

- `R6` The first release MUST keep the analysis bounded to one saved match. It
  MUST NOT silently change the existing aggregate dashboard contract in this
  feature.

- `R7` For eligible marksman-lane matches, the system MUST expose a bounded
  detailed metric set using only saved fields from the current match across the
  following categories:
  - `Match Context`
  - `Economy And Farming`
  - `Output And Pressure`
  - `Survival And Risk`
  - `Teamfight Presence`

- `R8` The first-release detailed metric set MUST stay within the following
  saved-field scope:
  - `Match Context`
    - `result`
    - `lane`
    - `score`
    - `kda`
  - `Economy And Farming`
    - `totalGold`
    - `goldShare`
    - `goldFromFarming`
    - `lastHits`
  - `Output And Pressure`
    - `damageDealt`
    - `damageShare`
    - `damageDealtToOpponents`
    - `score`
  - `Survival And Risk`
    - `kda`
    - `damageTaken`
    - `damageTakenShare`
  - `Teamfight Presence`
    - `participationRate`
    - `killParticipationCount`
    - `controlDuration`

- `R9` The system MUST NOT invent or estimate missing metric values. If one of
  the approved fields is empty, that metric MUST remain unavailable or partial
  in a user-facing way.

- `R10` Suggestions MUST be deterministic and traceable to explicit saved-match
  evidence. They MUST NOT be generic freeform coaching detached from the
  current saved record.

- `R11` The first release MUST keep the suggestion layer bounded to the
  following role-level categories:
  - `Economy Rhythm`
  - `Risk Discipline And Survival`
  - `Follow-Team / Isolation`
  - `Output Contribution`

- `R12` Each surfaced suggestion MUST include:
  - a user-facing title
  - a brief rationale
  - one visible evidence line tied to the current saved match data
  - the playbook rule category the suggestion belongs to

- `R13` The first release MUST keep the visible suggestion count bounded to at
  most 3 suggestions for one saved match so the screen remains readable.

- `R14` The system MUST surface an explicit neutral state that tells the user
  there are no high-priority marksman suggestions for this match when the match
  is eligible but no approved suggestion triggers fire.

- `R15` The marksman-lane analysis MUST keep the existing raw grouped record
  detail accessible. The new analysis layer MUST NOT replace or hide the saved
  field detail that already exists.

- `R16` The first release MUST distinguish three user-facing marksman analysis
  states:
  - `Eligible`
  - `Unavailable For This Lane`
  - `Insufficient Saved Data`

- `R17` The system MUST keep marksman-lane analysis logic outside leaf Compose
  UI code so the role rules, eligibility checks, and suggestion triggers remain
  testable as pure or near-pure logic.

## Invariants

- Existing saved-record viewing MUST continue to work even when no marksman
  analysis can be produced.
- The analysis MUST remain local-first and offline.
- The analysis MUST remain role-first, not hero-first, in this release.
- The user MUST always be able to tell whether missing insight means:
  - this was not a marksman-lane match
  - the saved data was insufficient
  - no high-priority suggestion was triggered

## Observability Map

The first release MUST treat playbook guidance in three buckets:

### Directly Observable Now

These are valid first-release inputs for marksman-lane metrics and suggestions:

- economy and farming from:
  - `totalGold`
  - `goldShare`
  - `goldFromFarming`
  - `lastHits`
- output and pressure from:
  - `damageDealt`
  - `damageShare`
  - `damageDealtToOpponents`
  - `score`
- survival and exposure from:
  - `kda`
  - `damageTaken`
  - `damageTakenShare`
- teamfight presence from:
  - `participationRate`
  - `killParticipationCount`
  - `controlDuration`

### Proxy-Observable Now

The first release MAY use bounded proxies, but MUST present them as inference
from current saved match data rather than as exact replay knowledge:

- `Follow-Team / Isolation` from participation-related saved fields
- `Risk Discipline And Survival` from KDA plus survival-related saved fields
- `Output Contribution` from damage/output-related saved fields

### Not Observable In This Release

The first release MUST NOT claim support for these playbook areas because the
current saved screenshot model does not retain enough evidence:

- anti-gank timing quality
- five-second information checks
- river / vision control decisions
- exact rotation quality
- exact objective timing and call quality
- kill-to-tower conversion history
- front-to-back positional correctness as a replay-accurate claim
- hero-specific matchup or build advice

## Error Handling And Boundary Behavior

- If one metric category cannot be produced because some inputs are missing, the
  system SHOULD keep the other independent categories visible when they remain
  valid.
- If marksman analysis computation fails internally for one match, the system
  MUST degrade to a visible unavailable/error state for the analysis layer
  rather than hiding the entire saved-record detail screen.
- Missing marksman analysis MUST NOT make the saved record look corrupted or
  unsaved.
- The marksman section MUST remain readable on a phone-sized detail screen and
  MUST NOT require horizontal scrolling.

## Compatibility And Migration Expectations

- The first release SHOULD work against already saved local matches with no
  schema migration when the approved fields are present.
- Older saved matches with partial optional fields MAY produce partial analysis.
- This feature MUST NOT require a new OCR template before the first release of
  marksman-lane insights ships.
- Existing non-marksman matches MUST continue to open normally in record
  detail, even though they do not receive marksman-lane analysis.

## Edge Cases

- eligible marksman-lane match with all optional analysis fields present
- eligible match with some optional analysis fields empty
- eligible match with enough data for metrics but not enough for some
  suggestion categories
- eligible match with no high-priority suggestion triggers
- eligible match where more than 3 suggestion triggers fire
- lane present but not `发育路`
- lane missing or unresolved
- internal analysis failure while saved raw detail is still readable
- older saved records created before this feature existed

## Non-Goals

- hero-specific handbooks or champion-mastery advice
- non-marksman lanes
- in-match or live coaching
- aggregate role coaching in the existing dashboard for this first release
- generative AI summaries
- replay-style claims about map movement, vision, anti-gank timing, or tower
  conversion that current saved data cannot prove

## Acceptance Criteria

- The detail screen can show a bounded marksman-lane insights section for
  eligible matches.
- The section uses only locally saved match data.
- The section shows bounded detailed metrics and bounded suggestions.
- The user can distinguish eligible, unavailable-for-this-lane, and
  insufficient-data states.
- Missing optional saved fields do not become fabricated metrics or fabricated
  coaching.
- Existing raw grouped record detail remains available.
- The current aggregate dashboard contract remains unchanged in this first
  feature release.
