# Step — Phase 1 : Structure core `.ai_context` v2

*Créé : 2026-05-15*

---

## Objectif

Aligner la structure `.ai_context/` générée par le plugin IntelliJ sur la v2 de référence :
- `tasks/` (remplace `documents/`)
- `steps/` (nouveau)
- `vision.md` (nouveau)
- `history.json` (remplace `history.log`)

---

## Features

- [x] `TemplateProvider.kt` : scaffoldInit avec vision + steps + tasks
- [x] `DddActions.kt` : flow init enrichi (vision + steps loop)
- [x] `DddToolWindowFactory.kt` : arborescence tasks/ + steps/ + skills/
- [x] `skills/` à la racine (bug corrigé)
- [x] `NewSkillAction` et `DddTreeCellRenderer` mis à jour
- [x] `.ai_context/` du plugin migré vers v2

---

## Notes

Parité avec le plugin VSCode atteinte. Les deux plugins produisent la même structure sur disque.

a