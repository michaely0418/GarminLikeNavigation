# GitHub workflow and owners

**Local source delivery only.** No GitHub remote or branch-protection rule was created by ChatGPT as part of this ZIP. Product owner chooses repo visibility and authorized collaborators.

## Branches
- `main` verified stable; `develop` integration; feature branches named `feature/<module>-<task>`.
- No direct pushes to protected branches after bootstrapping. Issues before code. Pull Requests with evidence, other-AI review, product owner approval for safety/policy changes.
- Use commit convention `feat:`, `fix:`, `test:`, `docs:`, `chore:`.

## Primary ownership
ChatGPT: M01/M02/M05 MapController/M06/M07 routing/M08/M10/M12/M15.
Gemini: M03/M04/M09/M11/M13/M14, plus road-law research for M07.
Shared `:core:model` + `:core:interfaces` changes require interface proposal and both reviewers.

## PR acceptance
- CI Android `assembleDebug` and model unit tests must actually run and produce logs.
- Don't merge unsafe or unsupported road-right rules, unreviewed place-data flow, secrets, or mismatched Android Auto surfaces.
- Route benchmark comparison must show data version, vehicle profile, map/routing engine version, legal evidence, route geometry/hash.
- No automatic merge by AI; product owner accepts Stage completion.

## Recommended GitHub settings
Private repo initially; require PR, 1 review (authorized human/account), check `Android Stage0 build`; enable secret scanning; choose branch protections. AI names are not literal CODEOWNERS accounts.
