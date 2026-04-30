# Spécification fonctionnelle — Dossier `skills/`

*Créé : 2026-04-30*

---

## Objectif

Ajouter un dossier `skills/` dans la structure `.ai_context/`, **côte à côte avec `documents/`**,
afin de permettre à l'équipe de capitaliser des savoir-faire métier et techniques réutilisables
d'un contexte à l'autre.

---

## Structure cible

```
.ai_context/
├── README.md
├── CONTRACT.md
├── CONTEXT.md
├── context.json
├── history.log
├── skills/                ← NOUVEAU — permanent-* conservés entre contextes
└── documents/
    ├── done/
    ├── specification/
    └── technical/
```

---

## Comportement attendu

### 1. Initialisation (`scaffoldInit`)
- Le dossier `skills/` est créé **à la racine de `.ai_context/`** (pas sous `documents/`).
- Un `.gitkeep` est ajouté pour que le dossier soit tracké par Git.

### 2. Passage de contexte (`scaffoldNewContext`)
- Les fichiers de `skills/` dont le nom commence par `permanent-` sont **conservés**.
- Les autres fichiers de `skills/` sont **supprimés** (comme pour `specification/` et `technical/`).

### 3. Arborescence (tree view)
- `skills/` apparaît comme nœud racine dans l'arborescence, **au même niveau que `documents/`**.
- Un clic droit sur `skills/` permet de créer un nouveau fichier `.md`.
- Les fichiers `permanent-*` sont affichés en **gras violet** (comme dans les autres dossiers).

### 4. Cohérence entre les deux plugins
- Le plugin **IntelliJ** et le plugin **VSCode** doivent produire la même structure sur disque.
- Le plugin VSCode utilise la commande générique `ddd.newDocument` (appliquée à tout dossier) — pas besoin de commande dédiée.

---

## Règles métier

| Règle | Détail |
|---|---|
| Position | `skills/` est à la racine de `.ai_context/`, pas dans `documents/` |
| Convention `permanent-` | Un fichier `permanent-foo.md` dans `skills/` survit aux changements de contexte |
| Pas de sous-dossiers imposés | `skills/` est un dossier plat (pas de `done/`, `specification/`, `technical/` dedans) |
| Comportement identique entre plugins | IntelliJ et VSCode produisent la même structure |

