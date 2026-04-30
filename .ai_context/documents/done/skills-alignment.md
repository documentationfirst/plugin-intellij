# Done — Ajout du dossier `skills/` et alignement plugins

*Complété : 2026-04-30*

---

## Ce qui a été fait

### 1. Correction du bug `scaffoldInit` — plugin IntelliJ
**Fichier modifié :** `plugin-intellij/src/main/kotlin/ai/documentationfirst/ddd/templates/TemplateProvider.kt`

**Problème :** la boucle d'initialisation incluait `"skills"` avec les autres sous-dossiers de `documents/`,
ce qui créait `documents/skills/` au lieu de `skills/` à la racine.

**Correction :** `skills` est sorti de la boucle et créé séparément à la racine de `.ai_context/`.

```kotlin
// Avant (bug)
for (sub in listOf("done", "specification", "technical", "skills")) {
    File(aiContextRoot, "documents/$sub").mkdirs()
}

// Après (corrigé)
for (sub in listOf("done", "specification", "technical")) {
    File(aiContextRoot, "documents/$sub").mkdirs()
}
File(aiContextRoot, "skills").mkdirs()
File(aiContextRoot, "skills/.gitkeep").also { if (!it.exists()) it.writeText("") }
```

---

### 2. Vérification de la cohérence — plugin VSCode
**Fichiers vérifiés :**
- `plugin-vscode/src/generator/TemplateGenerator.ts` → `scaffoldInit` et `scaffoldNewContext` : `skills/` déjà à la racine ✅
- `plugin-vscode/src/treeview/DddTreeProvider.ts` → affiche `skills/` côte à côte avec `documents/` ✅
- `plugin-vscode/package.json` → `ddd.newDocument` s'applique à tout `dddFolder`, y compris `skills/` ✅

Aucun changement nécessaire côté VSCode.

---

## Résultat

La structure produite par les deux plugins est désormais identique :

```
.ai_context/
├── README.md
├── CONTRACT.md
├── CONTEXT.md
├── context.json
├── history.log
├── skills/                ← à la racine, côte à côte avec documents/
└── documents/
    ├── done/
    ├── specification/
    └── technical/
```

