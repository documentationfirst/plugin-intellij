# Spécification : Architecture du contexte `.ai_context`

## Structure du dossier `.ai_context`

```
.ai_context/
├── README.md              ← guide humain : explique la structure du dossier
├── CONTRACT.md            ← profil du développeur, règles permanentes pour l'agent
├── CONTEXT.md             ← contexte actuel : titre, description, todo liste
├── context.json           ← données machine du contexte actuel (titre, description, date de début)
├── history.log            ← journal de tous les contextes passés (format JSON Lines)
└── documents/             ← l'historique complet est dans Git, pas dans des archives locales
    ├── done/              ← fichiers MD rédigés par l'agent résumant ce qui a été fait
    ├── specification/     ← détails fonctionnels, architecture, besoins
    └── technical/         ← bonnes pratiques et conseils techniques
```

---

## Fichiers racine

### `README.md`
Généré à l'initialisation. S'adresse à un développeur humain découvrant le projet.
Explique brièvement la structure du dossier `.ai_context` et comment l'utiliser.
**Ne pas confondre avec le `CONTRACT.md` qui s'adresse à l'agent.**

### `CONTRACT.md`
Contient les informations stables sur le développeur et ses règles permanentes pour l'agent :
- Profil du développeur (stack préférée, conventions, niveau d'expertise)
- Règles de comportement attendues de l'agent
- Contraintes du projet (licence, langue, style de code, etc.)

Ce fichier **n'est jamais archivé** lors du passage à un nouveau contexte. Il est conservé intact.

### `CONTEXT.md`
Contient le contexte de développement actuel :
- Titre du contexte
- Description (objectif, périmètre)
- Todo liste au format Markdown checkboxes (`- [ ] tâche`)

Mis à jour par le plugin lorsque l'utilisateur coche/décoche une tâche dans la fenêtre du plugin.
**Réinitialisé** lors du passage à un nouveau contexte.

### `context.json`
Données machine du contexte actuel :
```json
{
  "title": "Titre du contexte",
  "description": "Description courte",
  "startedAt": "2026-04-20T10:00:00Z"
}
```
Utilisé pour alimenter `history.log` lors de la clôture du contexte (avec ajout de `endedAt`).
**Réinitialisé** lors du passage à un nouveau contexte.

### `history.log`
Journal de tous les contextes passés au format **JSON Lines** (une entrée par ligne) :
```jsonl
{"title":"Mon premier contexte","description":"...","startedAt":"2026-04-01T09:00:00Z","endedAt":"2026-04-20T10:00:00Z"}
{"title":"Mon second contexte","description":"...","startedAt":"2026-04-20T10:00:00Z","endedAt":null}
```
Une nouvelle ligne est **ajoutée** (jamais remplacée) à chaque clôture de contexte.

---

## Sous-répertoires `documents/`

### Rôle
Ces trois répertoires contiennent les documents de travail du contexte courant.
Ils sont **vidés** lors du passage à un nouveau contexte (après archivage).

### `done/`
Fichiers rédigés par l'agent à la fin d'une tâche ou d'une session, résumant :
- Ce qui a été implémenté
- Les choix techniques effectués
- Les points d'attention pour la suite

> Le développeur doit **demander à l'agent** de rédiger ces fichiers en fin de session.

### `specification/`
Documents fonctionnels et d'architecture rédigés par le développeur, seul ou **en co-écriture avec l'agent** :
- Besoins détaillés
- Diagrammes ou descriptions d'architecture
- User stories, cas d'usage

Fichiers préfixés `permanent-` dans ce dossier (ex: `permanent-project-overview.md`) sont **conservés entre les contextes** et servent à présenter le projet dans sa globalité : vision produit, architecture générale, contraintes structurantes.

> Le développeur est encouragé à soumettre un brouillon à l'agent pour qu'il l'affine et le complète.

### `technical/`
Bonnes pratiques, décisions techniques et conseils rédigés par le développeur ou l'agent :
- ADR (Architecture Decision Records)
- Conventions de code spécifiques au contexte
- Notes sur les bibliothèques ou patterns utilisés

Fichiers préfixés `permanent-` dans ce dossier (ex: `permanent-coding-conventions.md`) sont **conservés entre les contextes** et représentent les règles et best-practices valables sur toute la durée du projet.

> Ces fichiers peuvent être initiés par l'agent sur demande ("documente cette décision technique").

### `done/`
Fichiers rédigés par l'agent à la fin d'une tâche ou d'une session. **Toujours contextuels**, jamais permanents : une synthèse de tâche appartient à son contexte.

> Le développeur doit **demander à l'agent** de rédiger ces fichiers en fin de session.

---

## Passage à un nouveau contexte

### Philosophie : Git comme archive
Il n'y a **pas d'archivage zip**. L'historique des contextes est délégué à **Git**.
Chaque contexte important doit faire l'objet d'un commit avant d'être remplacé.
Ainsi, chaque commit significatif du projet embarque naturellement son contexte AI.

> 💡 Un contexte = une unité de commit. C'est la philosophie "Documentation First" appliquée au versionnement.

### Avertissement obligatoire
Avant toute réinitialisation, le plugin **doit afficher une popup de confirmation** indiquant :

> ⚠️ **Êtes-vous sûr de vouloir démarrer un nouveau contexte ?**
>
> Les fichiers `CONTEXT.md`, `context.json` et le contenu de `documents/` vont être effacés.
> **Assurez-vous d'avoir commité ces fichiers** si vous souhaitez conserver ce contexte dans Git.
>
> `[Annuler]` `[Continuer quand même]`

### Fichiers conservés lors du changement de contexte
- `CONTRACT.md` → **conservé intact**
- `README.md` → **conservé intact**
- `history.log` → **conservé et enrichi**

### Fichiers réinitialisés
- `CONTEXT.md` → réécrit avec le nouveau contexte
- `context.json` → réécrit avec les nouvelles métadonnées
- `documents/done/` → **vidé intégralement**
- `documents/specification/*.md` → **vidé**, sauf les fichiers `permanent-*`
- `documents/technical/*.md` → **vidé**, sauf les fichiers `permanent-*`

### Séquence lors du passage à un nouveau contexte
1. Afficher la popup d'avertissement Git (voir ci-dessus)
2. Si l'utilisateur annule → arrêter
3. Lire `context.json` pour récupérer les métadonnées de l'ancien contexte
4. Ajouter une ligne à `history.log` avec `endedAt` = maintenant
5. Vider `documents/done/` entièrement, et vider `documents/specification/` et `documents/technical/` **en conservant les fichiers `permanent-*`**
6. Demander à l'utilisateur : titre, description, todo liste du nouveau contexte
7. Réécrire `CONTEXT.md` et `context.json` avec les nouvelles données
8. Rafraîchir l'arborescence dans la fenêtre du plugin

---

## Fenêtre du plugin

### Barre d'actions (haut)
- **"Initialiser le contexte"** : affiché uniquement si `.ai_context/` n'existe pas encore
- **"Nouveau contexte"** : affiché si le contexte est déjà initialisé

### Arborescence (milieu)
Affiche le contenu de `.ai_context/documents/` avec les trois sous-répertoires.
Un **clic droit** sur un sous-répertoire permet de créer un nouveau fichier `.md`.

**Règles de rendu visuel des fichiers :**

| Fichier | Affichage | Couleur |
|---|---|---|
| `specification/*.md` | Nom du fichier | Normale |
| `specification/permanent-*.md` | Nom **sans** le préfixe `permanent-`, en **gras** | Accentuée (ex: bleu) |
| `technical/*.md` | Nom du fichier | Normale |
| `technical/permanent-*.md` | Nom **sans** le préfixe `permanent-`, en **gras** | Accentuée (ex: bleu) |
| `done/*.md` | Nom du fichier | Neutre/atténuée (ex: gris) — signifie "archivé, lu seulement" |

### Panneau de contexte (bas)
Affiche en lecture/interaction :
- Date de début du contexte (`startedAt` de `context.json`)
- Titre
- Description
- Todo liste avec **cases à cocher cliquables** — chaque clic met à jour `CONTEXT.md`

