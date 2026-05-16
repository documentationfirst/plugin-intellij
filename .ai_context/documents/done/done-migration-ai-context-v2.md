# Done — Migration `.ai_context` v2 + enrichissement du flow init

*2026-05-15*

---

## Résumé

Même migration que pour le plugin VSCode.
Voir `plugin-vscode/.ai_context/documents/done/done-migration-ai-context-v2.md` pour le détail complet.

Points spécifiques au plugin IntelliJ :

### `TemplateProvider.kt`
- `aiContextReadme()` entièrement réécrit avec nouvelle structure
- `visionMd(vision: String)` et `stepMd(name, description)` ajoutés
- `scaffoldInit` : param `vision: String` + `steps: List<Pair<String,String>>`
- Création de `tasks/done`, `tasks/specification`, `tasks/technical`, `steps/` avec gitkeep
- `vision.md` créé si absent
- `scaffoldNewContext` : `history.json` au lieu de `history.log`, chemins `tasks/` au lieu de `documents/`

### `DddActions.kt`
- `InitContextAction` : dialog vision (JBTextField) + loop steps (2 champs nom + desc)
- Appel `scaffoldInit` avec les nouveaux params
- `NewContextAction` : message d'avertissement mis à jour

### `DddToolWindowFactory.kt`
- `buildTreeNode()` : boucle sur `["tasks", "steps", "skills"]`
- `DddTreeCellRenderer` : `ModelClass` pour `steps/`, `Package` pour `tasks/`
- Menu clic droit : pas de popup sur `tasks/` (container), popup normale sur ses sous-dossiers

### `.ai_context`
- `README.md` réécrit, `CONTEXT.md` mis à jour, `vision.md` créé
- `steps/` et `tasks/` à créer via les commandes bash dans `CONTEXT.md`

