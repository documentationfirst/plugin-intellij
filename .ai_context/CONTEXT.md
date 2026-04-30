# Contexte — Ajout du dossier `skills/` et alignement plugins

*Démarré : 2026-04-30*

---

## Description

Ajout du dossier `skills/` dans la structure `.ai_context/`, côte à côte avec `documents/`.
Correction du bug dans le plugin IntelliJ (`scaffoldInit` créait `documents/skills/` au lieu de `skills/`).
Le plugin VSCode avait déjà la bonne position pour `skills/` mais n'avait pas de `newSkill` dédié —
la fonctionnalité est assurée par le `ddd.newDocument` générique qui s'applique à tout dossier.

---

## Todo

- [x] Corriger `scaffoldInit` dans `TemplateProvider.kt` : `skills/` à la racine, pas sous `documents/`
- [x] Vérifier la cohérence VSCode : `scaffoldInit`, `scaffoldNewContext`, `DddTreeProvider` — OK
- [x] `DddTreeCellRenderer` : icône distincte `Editorconfig` pour `skills/`
- [x] Ajouter `NewSkillAction` dans `DddActions.kt` avec template dédié
- [x] Menu clic droit : "New skill .md" sur `skills/`, "New file .md" sur les autres dossiers
- [x] Rédiger `specs-functional.md` dans `documents/specification/`
- [x] Rédiger le fichier `done/` résumant les changements effectués
