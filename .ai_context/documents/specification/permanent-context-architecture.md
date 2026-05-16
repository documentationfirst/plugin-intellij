# Spécification : Architecture du contexte `.ai_context`

## Structure du dossier `.ai_context`

```
.ai_context/
├── README.md              ← guide humain : explique la structure du dossier
├── CONTRACT.md            ← profil du développeur, règles permanentes pour l'agent
├── CONTEXT.md             ← contexte actuel : titre, description, todo liste
├── context.json           ← données machine du contexte actuel (titre, description, date de début)
├── vision.md              ← vision produit et objectifs épiques (permanent)
├── history.json           ← journal de tous les contextes passés (format JSON Lines, permanent)
├── skills/                ← comportements permanents de l'agent (permanent-* conservés)
├── steps/                 ← jalons / features du roadmap (permanent)
└── tasks/                 ← l'historique complet est dans Git, pas dans des archives locales
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
Utilisé pour alimenter `history.json` lors de la clôture du contexte (avec ajout de `endedAt`).
**Réinitialisé** lors du passage à un nouveau contexte.

### `history.json`
Journal de tous les contextes passés au format **JSON Lines** (une entrée par ligne) :
```jsonl
{"title":"Mon premier contexte","description":"...","startedAt":"2026-04-01T09:00:00Z","endedAt":"2026-04-20T10:00:00Z"}
{"title":"Mon second contexte","description":"...","startedAt":"2026-04-20T10:00:00Z","endedAt":null}
```
Une nouvelle ligne est **ajoutée** (jamais remplacée) à chaque clôture de contexte.

### `vision.md`
Vision produit et objectifs épiques du projet. Créé à l'initialisation, **jamais réinitialisé**.

### `steps/`
Jalons et features du roadmap. Fichiers permanents représentant les phases de développement.
**Jamais réinitialisé** lors du passage à un nouveau contexte.

---

## Sous-répertoires `tasks/`

### Rôle
Ces trois répertoires contiennent les documents de travail du contexte courant.
Ils sont **vidés** lors du passage à un nouveau contexte (après archivage).
`tasks/` ne contient **jamais** de fichiers directement à sa racine — uniquement des sous-dossiers.

### `tasks/done/`
Fichiers rédigés par l'agent à la fin d'une tâche ou d'une session. **Toujours contextuels**.

### `tasks/specification/`
Documents fonctionnels et d'architecture. Fichiers préfixés `permanent-` **conservés entre les contextes**.

### `tasks/technical/`
Bonnes pratiques et décisions techniques. Fichiers préfixés `permanent-` **conservés entre les contextes**.

---

## Passage à un nouveau contexte

### Philosophie : Git comme archive
Il n'y a **pas d'archivage zip**. L'historique des contextes est délégué à **Git**.

> 💡 Un contexte = une unité de commit. C'est la philosophie "Documentation First" appliquée au versionnement.

### Avertissement obligatoire
Avant toute réinitialisation, le plugin **doit afficher une popup de confirmation** :

> ⚠️ Les fichiers `CONTEXT.md`, `context.json` et le contenu de `tasks/` vont être effacés.
> `vision.md` et `steps/` sont **conservés**.
> **Commitez d'abord** si vous souhaitez conserver ce contexte dans Git.

### Fichiers conservés lors du changement de contexte
- `CONTRACT.md`, `README.md`, `vision.md`, `steps/`, `skills/` → **conservés intacts**
- `history.json` → **conservé et enrichi**

### Fichiers réinitialisés
- `CONTEXT.md`, `context.json` → réécrits
- `tasks/done/` → **vidé intégralement**
- `tasks/specification/*.md` → **vidé**, sauf `permanent-*`
- `tasks/technical/*.md` → **vidé**, sauf `permanent-*`

### Séquence lors du passage à un nouveau contexte
1. Popup d'avertissement Git
2. Si annulation → arrêter
3. Lire `context.json`, ajouter une ligne à `history.json` avec `endedAt`
4. Vider `tasks/done/` et les non-`permanent-*` de `tasks/specification/` et `tasks/technical/`
5. Demander : titre, description, todo liste
6. Réécrire `CONTEXT.md` et `context.json`
7. Rafraîchir l'arborescence

---

## Fenêtre du plugin

### Barre d'actions (haut)
- **"Initialize a context"** : si `.ai_context/` n'existe pas encore
- **"+ New context"** : si le contexte est déjà initialisé

### Arborescence (milieu)
Affiche `tasks/`, `steps/`, `skills/` avec icônes distinctes.
Clic droit sur un sous-dossier → créer un nouveau fichier `.md`.

**Règles de rendu visuel :**

| Fichier | Affichage | Couleur |
|---|---|---|
| `permanent-*.md` | Nom sans préfixe, en **gras** | Violet |
| `tasks/done/*.md` | Nom du fichier | Gris atténué |
| Autres `.md` | Nom du fichier | Normale |

### Panneau de contexte (bas)
- Date de début, titre, description
- Todo liste avec **cases à cocher cliquables** — chaque clic met à jour `CONTEXT.md`

