# Done — Migration `.ai_context` v2 (IntelliJ)

*2026-05-15*

Migré depuis `documents/done/done-migration-ai-context-v2.md`.

## Changements

### `TemplateProvider.kt`
- `visionMd()` + `stepMd()` ajoutés
- `scaffoldInit` : param `vision` + `steps: List<Pair<String,String>>`
- Création de `tasks/done`, `tasks/specification`, `tasks/technical`, `steps/`
- `vision.md` créé si absent
- `scaffoldNewContext` : `history.json`, chemins `tasks/`

### `DddActions.kt`
- `InitContextAction` : dialog vision + loop steps
- `NewContextAction` : avertissement mis à jour

### `DddToolWindowFactory.kt`
- Arborescence : `tasks/`, `steps/`, `skills/`
- Icônes : `ModelClass` (steps), `Package` (tasks), `Editorconfig` (skills)
- Pas de popup sur `tasks/` — container uniquement

