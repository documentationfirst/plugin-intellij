# `.ai_context` — Documentation-Driven Development v2

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

## Comment travailler avec ce dossier

### 1. Lire avant d'agir
Avant de démarrer une session de travail, l'agent **doit lire** :
- `CONTRACT.md` → les règles permanentes du projet et du développeur
- `CONTEXT.md` → l'objectif et les tâches du contexte en cours
- `documents/specification/` → les détails fonctionnels et architecturaux
- `documents/technical/` → les décisions techniques et bonnes pratiques

### 2. Travailler par ajout de documents contextuels
Chaque nouveau besoin, décision ou découverte doit se traduire par **l'ajout ou la mise à jour d'un fichier** dans `documents/`.
- Une nouvelle contrainte technique → un fichier dans `technical/`
- Un besoin fonctionnel précisé → un fichier dans `specification/`
- Une tâche terminée → un fichier de synthèse dans `done/`

Le développeur peut créer ces fichiers lui-même, les co-écrire avec l'agent, ou demander à l'agent de les produire.

### Convention `permanent-`
Un fichier dans `technical/` ou `specification/` préfixé par `permanent-` **ne sera pas vidé** lors du passage à un nouveau contexte.

Exemples :
- `permanent-architecture-overview.md` → présentation globale du projet, valable sur toute sa durée
- `permanent-coding-conventions.md` → règles de code spécifiques au projet
- `permanent-stack-decisions.md` → choix technologiques structurants

> Dans la fenêtre du plugin, ces fichiers sont affichés avec une mise en forme distincte (gras ou couleur).
> Le préfixe `permanent-` est masqué dans l'affichage pour plus de lisibilité.

⚠️ Cette convention ne s'applique **pas** à `done/` — une synthèse de tâche appartient toujours à son contexte.

### 3. Clore un contexte avant de commiter
Un **contexte = une unité de travail = un commit Git**.
Avant de passer à un nouveau contexte, le développeur doit commiter `.ai_context/`
pour conserver l'historique documentaire dans Git.

---

## Structure du dossier

```
.ai_context/
├── README.md              ← ce fichier (permanent)
├── CONTRACT.md            ← règles permanentes pour l'agent (permanent)
├── CONTEXT.md             ← objectif et todo liste du contexte actuel (réinitialisé à chaque contexte)
├── context.json           ← métadonnées machine du contexte actuel (réinitialisé à chaque contexte)
├── history.log            ← journal de tous les contextes passés en JSON Lines (permanent)
└── documents/
    ├── done/              ← synthèses rédigées par l'agent (vidé à chaque contexte, jamais permanent)
    ├── specification/     ← besoins fonctionnels et architecture (vidé à chaque contexte)
    │   └── permanent-*.md ← fichiers préfixés : conservés entre les contextes
    └── technical/         ← décisions techniques et bonnes pratiques (vidé à chaque contexte)
        └── permanent-*.md ← fichiers préfixés : conservés entre les contextes
```

---

## Fichiers permanents vs contextuels

| Fichier / Dossier | Type | Rôle |
|---|---|---|
| `README.md` | Permanent | Ce guide |
| `CONTRACT.md` | Permanent | Règles de l'agent |
| `history.log` | Permanent (enrichi) | Journal des contextes |
| `CONTEXT.md` | Contextuel | Objectif en cours |
| `context.json` | Contextuel | Métadonnées machine |
| `documents/done/` | Contextuel | Synthèses de l'agent |
| `documents/specification/*.md` | Contextuel | Specs du contexte en cours |
| `documents/specification/permanent-*.md` | **Permanent** | Présentation projet, architecture globale |
| `documents/technical/*.md` | Contextuel | Décisions techniques du contexte |
| `documents/technical/permanent-*.md` | **Permanent** | Conventions et best-practices projet |

---

*Géré par [Documentation First Plugin](https://documentationfirst.ai) — MIT License*

