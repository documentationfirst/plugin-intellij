# Spécification fonctionnelle — Structure `.ai_context` v2

*Mise à jour : 2026-05-15*

---

## Objectif

Définir la structure de référence du dossier `.ai_context/` utilisée par les deux plugins
(VSCode et IntelliJ), telle que décrite dans `skills/methodology-ddd-ai.md`.

---

## Structure cible

```
.ai_context/
├── README.md              ← guide humain (permanent)
├── CONTRACT.md            ← règles pour l'agent (permanent)
├── CONTEXT.md             ← contexte actuel : objectif et todo list (contextuel)
├── context.json           ← métadonnées machine (contextuel)
├── vision.md              ← vision produit et objectifs épiques (permanent)
├── history.json           ← journal des contextes passés en JSON Lines (permanent)
├── skills/                ← comportements permanents de l'agent (permanent-* conservés)
├── steps/                 ← jalons / features du roadmap (permanent)
└── tasks/
    ├── done/              ← comptes-rendus de l'agent (contextuel)
    ├── specification/     ← specs fonctionnelles (permanent-* conservés)
    └── technical/         ← décisions techniques (permanent-* conservés)
```

---

## Comportement attendu

### 1. Initialisation (`scaffoldInit`)

Flow en 5 étapes :
1. Profil développeur → `CONTRACT.md`
2. Vision produit → `vision.md`
3. Titre + description du contexte → `CONTEXT.md` + `context.json`
4. Steps (loop) → `steps/<name>.md`
5. Todos (loop) → `CONTEXT.md`

Dossiers créés : `tasks/done/`, `tasks/specification/`, `tasks/technical/`, `steps/`, `skills/`

### 2. Passage de contexte (`scaffoldNewContext`)

- Archive le contexte courant dans `history.json` (JSON Lines)
- Vide `tasks/done/` entièrement
- Supprime les non-`permanent-*` de `tasks/specification/` et `tasks/technical/`
- Conserve `vision.md`, `steps/`, `skills/`, `CONTRACT.md`, `README.md`
- Réinitialise `CONTEXT.md` et `context.json`

### 3. Arborescence (tree view)

- Racine : `tasks/`, `steps/`, `skills/` avec icônes distinctes
- `skills/` : icône `Editorconfig` (IntelliJ)
- `steps/` : icône `ModelClass` (IntelliJ)
- `tasks/` : icône `Package` (IntelliJ), pas de popup — container uniquement
- `permanent-*.md` : en gras violet

### 4. Cohérence entre les deux plugins

- IntelliJ et VSCode produisent une structure sur disque identique
- `tasks/` remplace l'ancien `documents/`

---

## Règles métier

| Règle | Détail |
|---|---|
| `tasks/` jamais direct | Uniquement des sous-dossiers à la racine de `tasks/` |
| Convention `permanent-` | Fichier préfixé `permanent-` survit aux changements de contexte |
| Git comme archive | Pas d'archivage zip — l'historique est dans Git |
| Un contexte = un commit | Philosophie DDD appliquée au versionnement |

