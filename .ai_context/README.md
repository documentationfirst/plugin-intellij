# `.ai_context` — Documentation-Driven Development

Ce dossier est géré par le plugin **Documentation First** (JetBrains).
Il structure la collaboration entre le développeur et l'agent IA tout au long du projet.

---

## Philosophie

La méthode repose sur un principe simple : **la documentation n'est pas un livrable, c'est un outil de travail**.

Le développeur et l'agent IA collaborent par **lecture et écriture de documents**.
- Le développeur écrit ce qu'il veut faire, comprend ou décide.
- L'agent lit, complète, affine, questionne, documente ce qu'il a fait.
- Ensemble, ils maintiennent une base documentaire vivante qui sert de contexte à chaque tâche du projet.

> Ce n'est pas l'agent qui décide — c'est le développeur qui pilote via les documents.

---

## Ordre de lecture (avant toute action)

1. `README.md` — ce fichier
2. `CONTEXT.md` — focus sprint/tâche en cours
3. `CONTRACT.md` — règles d'interaction
4. `vision.md` — vision produit et objectifs épiques
5. `skills/` — comportements permanents de l'agent
6. `steps/` — jalons et features du roadmap
7. `tasks/specification/` — specs actives du contexte courant
8. `tasks/done/` — ce qui a déjà été implémenté
9. `tasks/technical/` — références techniques permanentes

---

## Convention `permanent-`

Un fichier préfixé `permanent-` **ne sera pas supprimé** lors du passage à un nouveau contexte.
S'applique à `tasks/specification/`, `tasks/technical/`, et `skills/`.

## Structure du dossier

```
.ai_context/
├── README.md              ← ce fichier (permanent)
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

## Deux niveaux de contexte

| Niveau | Emplacement | Nature | Reset ? |
|--------|-------------|--------|---------|
| **Contexte général** | `CONTEXT.md` + `vision.md` + `CONTRACT.md` + `steps/` + `skills/` | Permanent | ❌ Jamais |
| **Contexte de développement** | `tasks/` (sous-dossiers uniquement) | Lié à un sprint | ✅ Possible |

> `tasks/` ne contient **jamais** de fichiers directement à sa racine — uniquement des sous-dossiers.

---

*Géré par [Documentation First Plugin](https://documentationfirst.ai) — MIT License*
