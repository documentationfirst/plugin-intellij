# Done — Port VSCode : refonte .ai_context

*Complété : 2026-04-20*

---

## Résumé

Port complet du plugin JetBrains v2 vers VSCode. Nouveau modèle `.ai_context/` + `CONTRACT.md`, nouveau flux de contexte, UI en 3 panneaux (Actions / Documents / Context), tests mis à jour.

---

## Fichiers modifiés

| Fichier | Nature du changement |
|---|---|
| `src/utils/fileUtils.ts` | `aiMdFilesPath` → `aiContextPath`, ajout `appendFile`, `nowIso`, support `DDD_TEST_ROOT` |
| `src/detector/DddDetector.ts` | `ai_md_files` → `.ai_context` |
| `src/generator/TemplateGenerator.ts` | Réécriture complète — `scaffoldInit`, `scaffoldNewContext`, `contractMd`, `contextMd`, `contextJson` |
| `src/treeview/DddTreeProvider.ts` | Affiche `.ai_context/documents/`, `permanent-` en violet, `done/` en gris |
| `src/extension.ts` | Réécriture — 3 WebviewViews, watcher `.ai_context`, `ddd.initialized` context key |
| `package.json` | v2.0.0, 3 vues sidebar (`dddActionPanel`, `dddExplorer`, `dddContextPanel`) |
| `test/suite/extension.test.ts` | Réécriture complète pour le nouveau modèle |

## Fichiers créés

| Fichier | Rôle |
|---|---|
| `src/commands/initContext.ts` | Profil + titre + description + todos en loop |
| `src/commands/newContext.ts` | Warning Git + archivage history.log + reset |
| `src/commands/newDocument.ts` | Crée un `.md` dans un sous-dossier via clic droit |
| `src/webview/ActionPanel.ts` | Section "ACTIONS" — bouton orange "⚡ Initialize Context" ou bouton bleu "＋ New Context" selon état |
| `src/webview/ContextPanel.ts` | Section "CONTEXT" — titre, description, date, todos cliquables synchro `CONTEXT.md` |

---

## Architecture sidebar finale

```
┌─────────────────────────────┐
│  ACTIONS                    │
│  [⚡ Initialize Context]    │  ← orange si non initialisé
│  [＋ New Context]           │  ← bleu VSCode si initialisé
├─────────────────────────────┤
│  DOCUMENTS              🔄  │
│  ▸ done/                    │
│  ▸ specification/           │
│  ▸ technical/               │
├─────────────────────────────┤
│  CONTEXT                    │
│  **Titre** — 2026-04-20     │
│  description...             │
│  ☐ Todo 1                   │
│  ☑ Todo 2                   │
└─────────────────────────────┘
```

---

## Décisions techniques

- **`ActionPanel` WebviewView séparée** : seul moyen d'afficher un bouton avec texte visible en permanence dans la sidebar VSCode. Les `view/title` buttons n'affichent que des icônes.
- **`ddd.initialized` context key** : mis à jour au démarrage, après chaque init/nouveau contexte, et via le watcher. Permet de conditionner la visibilité des boutons sans polling.
- **Todos en loop** : VSCode InputBox est monoligne — on demande les tâches une par une jusqu'à saisie vide.
- **`DDD_TEST_ROOT`** : variable d'environnement pour court-circuiter `vscode.workspace` dans les tests Node.js purs.

---

## Points d'attention

- `initProject.ts`, `newFeatureContext.ts`, `newMigrationPlan.ts`, `viewDone.ts`, `AgentFileGenerator.ts`, `WelcomePanel.ts` sont orphelins — à supprimer lors d'un prochain nettoyage.
- Interopérabilité garantie avec JetBrains : formats `CONTEXT.md`, `context.json`, `history.log` identiques.

