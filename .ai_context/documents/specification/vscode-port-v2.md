# Spécification — Port VSCode : refonte .ai_context

*Rédigé : 2026-04-20*

Ce document décrit toutes les nouveautés implémentées dans le plugin JetBrains v2,
à porter sur le plugin VSCode.

---

## 1. Changement de dossier racine

| Avant | Après |
|---|---|
| `ai_md_files/` à la racine du projet | `.ai_context/` à la racine du projet |

Le dossier est désormais **caché** (préfixe `.`) — convention Unix pour l'outillage.

---

## 2. Suppression de `README_AI.md`

`README_AI.md` n'existe plus. Il est remplacé par :

- **Un paragraphe en tête du `README.md`** du projet, qui déclare à l'agent la présence du dossier `.ai_context/` et lui demande de le lire. Exemple :
  ```markdown
  # For AI Agent :
  Read all [context](./.ai_context) for context and needs.
  L'agent doit appliquer les conditions spécifiées par le fichier CONTRACT.md
  ...
  ```
- **`.ai_context/CONTRACT.md`** : contient les règles permanentes de l'agent (profil, permissions, langue). Généré à l'init selon le profil choisi (Strict / Standard / Permissive). **N'est jamais effacé** lors du changement de contexte.

---

## 3. Nouvelle structure `.ai_context/`

```
.ai_context/
├── README.md              ← guide humain permanent (généré à l'init si absent)
├── CONTRACT.md            ← règles agent permanent (généré à l'init si absent)
├── CONTEXT.md             ← contexte actuel : titre, description, todos (contextuel)
├── context.json           ← métadonnées machine du contexte actuel (contextuel)
├── history.log            ← journal JSON Lines des contextes passés (permanent)
└── documents/
    ├── done/              ← synthèses de l'agent, toujours contextuelles
    ├── specification/     ← specs fonctionnelles (permanent-* conservés)
    └── technical/         ← décisions techniques (permanent-* conservés)
```

---

## 4. Format de `CONTEXT.md`

Généré automatiquement à l'initialisation et au changement de contexte :

```markdown
# Contexte — {titre}

*Démarré : {date}*

---

## Description

{description}

---

## Todo

- [ ] tâche 1
- [ ] tâche 2
```

Le plugin doit parser les lignes `- [ ] ...` et `- [x] ...` pour afficher
les todos comme cases à cocher interactives.

---

## 5. Format de `context.json`

```json
{"title":"Mon titre","description":"Ma description","startedAt":"2026-04-20T10:00:00Z"}
```

- `startedAt` : ISO 8601 UTC
- Pas d'indentation — une seule ligne

---

## 6. Format de `history.log`

JSON Lines — une entrée par ligne, ajoutée à chaque clôture de contexte :

```jsonl
{"title":"Contexte 1","description":"...","startedAt":"2026-04-01T09:00:00Z","endedAt":"2026-04-20T10:00:00Z"}
{"title":"Contexte 2","description":"...","startedAt":"2026-04-20T10:00:00Z","endedAt":null}
```

---

## 7. Convention `permanent-`

Dans `documents/specification/` et `documents/technical/` uniquement :

- Tout fichier préfixé `permanent-` (ex: `permanent-architecture.md`) est **conservé** lors du changement de contexte.
- Les autres fichiers sont supprimés.
- `documents/done/` : **jamais de fichiers permanents** — tout est contextuel.

**Rendu visuel dans le plugin :**
- Fichiers `permanent-*` : affichés **sans le préfixe**, en **gras** et couleur violette (`#8C52C8`).
- Fichiers dans `done/` : affichés en **gris/atténué**.
- Fichiers normaux : style par défaut.

---

## 8. Flux d'initialisation

**Commande / action : "Initialize Context"**

1. Afficher uniquement si `.ai_context/` n'existe pas encore.
2. Demander le profil agent (Strict / Standard / Permissive).
3. Demander : titre, description, todo liste (une tâche par ligne).
4. Créer la structure `.ai_context/` complète.
5. Écrire `README.md` et `CONTRACT.md` dans `.ai_context/` (seulement si absents).
6. Gérer le `README.md` à la **racine du projet** :
   - S'il est **absent** → le créer avec le paragraphe agent en en-tête + placeholder titre du projet.
   - S'il est **présent mais ne contient pas `.ai_context`** → **insérer le paragraphe agent en tête** sans toucher au reste du contenu.
   - S'il est **présent et contient déjà `.ai_context`** → ne rien faire (évite les doublons).

   Paragraphe agent à insérer :
   ```markdown
   # For AI Agent :

   Read all [context](./.ai_context) for context and needs.
   L'agent doit appliquer les conditions spécifiées par le fichier CONTRACT.md
   Le contexte du développement actuel est présenté dans CONTEXT.md et tous les fichiers du répertoire `documents/`.

   ---

   ```
7. Écrire `CONTEXT.md` et `context.json`.
7. Rafraîchir la vue du plugin.

---

## 9. Flux de changement de contexte

**Commande / action : "New Context"**

1. Afficher uniquement si `.ai_context/` existe.
2. Afficher une **popup de confirmation** :
   > ⚠️ Les fichiers CONTEXT.md, context.json et le contenu de documents/ vont être effacés (sauf les fichiers permanent-*).
   > Assurez-vous d'avoir commité ces fichiers dans Git.
   > `[Annuler]` `[Continuer quand même]`
3. Si annulé → stop.
4. Lire `context.json`, ajouter une ligne dans `history.log` avec `endedAt` = maintenant.
5. Vider `documents/done/` entièrement.
6. Vider `documents/specification/` et `documents/technical/` **sauf** les `permanent-*` et `.gitkeep`.
7. Demander : titre, description, todo liste du nouveau contexte.
8. Réécrire `CONTEXT.md` et `context.json`.
9. Rafraîchir la vue du plugin.

---

## 10. Vue du plugin (sidebar / panel)

### Zone haute — actions
- Si non initialisé : bouton orange **"⚡ Initialiser le contexte"**
- Si initialisé : bouton **"+ Nouveau contexte"**

### Zone centrale — arborescence
- Affiche le contenu de `.ai_context/documents/`
- Les 3 sous-dossiers sont toujours visibles
- Clic sur un fichier → ouvre dans l'éditeur
- **Clic droit sur un dossier** → menu "Nouveau fichier .md" → demande un nom → crée le fichier

### Zone basse — panneau de contexte
Lecture depuis `context.json` et `CONTEXT.md` :
- **Titre** (gras) + **date de début** (gris)
- **Description** (italique, atténuée)
- **Todo liste** : cases à cocher cliquables
  - Cocher → remplace `- [ ] tâche` par `- [x] tâche` dans `CONTEXT.md`
  - Décocher → inverse

---

## 11. Détection au démarrage

À l'ouverture d'un projet :
- Si `.ai_context/` existe → notification "Prêt ✅" avec la stack détectée
- Si `.ai_context/` absent → notification "Aucun contexte" avec bouton "Initialiser"

La détection de stack (Angular, React, Vue, Spring Boot, Python, Rust, Go, Generic)
reste inchangée — basée sur les fichiers de configuration du projet.

