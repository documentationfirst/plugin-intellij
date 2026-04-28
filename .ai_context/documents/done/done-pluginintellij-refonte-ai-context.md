# Done — Refonte architecture .ai_context (JetBrains plugin)

*Complété : 2026-04-20*

---

## Résumé

Refonte complète du plugin JetBrains Documentation First. Le modèle `ai_md_files/` a été abandonné au profit d'un dossier `.ai_context/` caché, avec un nouveau concept de **contexte de développement** (titre, description, todo liste, documents). L'UI du tool window a été entièrement redessinée. Les tests ont été mis à jour en conséquence.

---

## Changements effectués

### Fichiers modifiés

| Fichier | Nature du changement |
|---|---|
| `TemplateProvider.kt` | Réécriture complète — `scaffoldInit`, `scaffoldNewContext`, `contractMd`, `contextMd`, `contextJson`, `aiContextReadme` |
| `DddDetector.kt` | `ai_md_files` → `.ai_context` |
| `ActionUtils.kt` | `requireAiMdRoot` → `requireAiContextRoot` + nouveau dialog `askContextInput` (titre, description, todos) |
| `DddActions.kt` | Remplacement de toutes les actions par `InitContextAction` + `NewContextAction` + `NewDocumentAction` |
| `DddNotifications.kt` | Messages en français, référence `.ai_context`, action `ddd.initContext` |
| `DddToolWindowFactory.kt` | Nouveau layout : barre d'actions, arborescence `documents/`, panneau contexte interactif en bas |
| `DddFileWatcher.kt` | Surveillance de `.ai_context` au lieu de `ai_md_files`, ID tool window corrigé |
| `plugin.xml` | Nom "Documentation First", actions `ddd.initContext` + `ddd.newContext` uniquement |
| `TemplateProviderTest.kt` | Réécriture complète pour le nouveau modèle |
| `AgentComplianceTest.kt` | `readmeAi()` → `contractMd()`, assertions adaptées au français |
| `DddDetectorTest.kt` | `ai_md_files` → `.ai_context` |

### Fichiers créés dans `.ai_context/`

| Fichier | Rôle |
|---|---|
| `README.md` | Guide humain permanent de la structure |
| `CONTRACT.md` | Contrat agent existant mis à jour |
| `documents/specification/context-architecture.md` | Spécification complète de l'architecture |
| `documents/specification/vscode-port-v2.md` | Spécification pour le portage VSCode |

---

## Décisions techniques

- **Pas de zip** : l'archivage des contextes est délégué à Git. Un contexte = une unité de commit.
- **`permanent-` comme convention de fichier** : simple, lisible dans Git, pas de config supplémentaire. Applicable uniquement dans `specification/` et `technical/`.
- **`history.log` en JSON Lines** : lisible machine, appendable sans parsing, compatible avec tout outil de log.
- **`context.json` en une ligne** : format minimal contrôlé par le plugin, pas de dépendance JSON externe.
- **`ActionManager.tryToExecute()`** : remplace l'API `ActionUtil.invokeAction()` dépréciée pour déclencher les actions depuis les composants Swing (IJ 2024.3+).

---

## Points d'attention pour la suite

- Le portage VSCode doit respecter exactement les formats `CONTEXT.md`, `context.json` et `history.log` décrits dans `vscode-port-v2.md` pour garantir l'interopérabilité entre les deux plugins.
- Le `context.json` est actuellement vide dans ce projet — il faudra l'initialiser via l'action "Initialize Context" du plugin avant de commencer le prochain contexte.
- La todo liste cliquable dans le panneau bas rafraîchit `CONTEXT.md` sur disque mais **ne rafraîchit pas automatiquement** la vue si le fichier est ouvert dans l'éditeur — à surveiller pour une future version.

