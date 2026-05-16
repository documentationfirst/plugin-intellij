# Agent Skill — Documentation-Driven Development

---

## 🗂️ Structure `ai_context` — à lire avant toute action

```
.ai_context/
  ├── README.md              ← Porte d'entrée — lire en premier
  ├── CONTEXT.md               ← État actuel du projet (stack, versions, décisions prises)
  ├── CONTRACT.md               ← Limites de l'agent
  ├── vision.md                ← Où on veut aller (produit, business, légal)
  ├── history.json               ← liste des visions, steps et tasks achevées
  ├── steps/                 ← Jalons et phases de développement
  │     phase1-suite.md
  │     phase2.md
  │     legal-gouvernance.md
  │     croissance-go-to-market.md
  │     ...
  ├── skills/                ← Comportements permanents de l'agent
  │     methodology.md
  │     ...
  └── tasks/                 ← Livrables de développement — sous-dossiers uniquement
        specification/       ← Specs actives du contexte courant
            phase1-suite-intro.md
            phase1-suite-intro-preview.html
        done/                ← Comptes-rendus d'implémentation
            phase1-suite-intro.html
            phase1-suite-intro-test.html
        technical/           ← Références techniques permanentes
              source-app-reference.md
              backend-specs.md
              best-practices.md
              ...
```

### Deux niveaux de contexte

| Niveau | Emplacement | Nature | Reset ? |
|--------|-------------|--------|---------|
| **Contexte général** | `CONTEXT.md` + `vision.md` + `CONTRACT.md` + `steps/` + `skills/` | Permanent — ne pas modifier sans accord explicite | ❌ Jamais |
| **Contexte de développement** | `tasks/` (sous-dossiers uniquement) | Lié à un sprint / un contexte de travail | ✅ Possible |

> `tasks/` ne contient **jamais** de fichiers directement à sa racine — uniquement des sous-dossiers.
> `tasks/technical/` contient des références **permanentes** (pas de reset).

### Convention `permanent-`

Dans `tasks/`, les fichiers préfixés `permanent-` survivent à tout reset de contexte.

---

## 🔁 Protocole de session — dans cet ordre, sans exception

### 1. 📖 Lire les fichiers de contexte

Avant toute action, lire dans cet ordre :

1. `README.md` (arborescence + ordre de lecture)
2. `CONTEXT.md` (état technique actuel)
3. `CONTRACT.md` (règles d'interaction)
4. `vision.md` (produit & objectifs)
5. `skills/methodology.md` (ce fichier)
6. `steps/` → phase(s) concernée(s) par la session
7. `tasks/specification/` → specs actives
8. `tasks/done/` → ce qui a déjà été implémenté
9. `tasks/technical/` → références techniques permanentes

> **Ces fichiers sont la mémoire du projet.** Ne jamais supposer l'état du code sans les avoir lus.

---

### 2. 📐 Rédiger la spec AVANT tout code

Pour toute nouvelle feature, page, service ou refactoring :

1. Créer `tasks/specification/spec-<phase>-<feature>.md`
2. Attendre la **validation explicite** du développeur ("ok", "go", "c'est bon"…)
3. **Ne jamais commencer à coder sans cette validation**

> Si le développeur donne des précisions ou corrections, les intégrer dans le contexte uniquement — **ne déclencher aucune action** (code, fichier, outil) sauf demande explicite.

#### Structure minimale d'une spec

```markdown
# 🎯 Spec — <Nom de la feature>

> Date : YYYY-MM-DD
> Phase : X.Y
> Route / Composant : /xxx

## Contexte & objectif
## Comportement attendu
## Composants / Services touchés
## Modèle de données
## Algorithmes clés
## Hors scope
## Plan d'implémentation
```

---

### 3. 🛠️ Implémenter

- Suivre la spec validée **à la lettre**
- Grouper les modifications par fichier
- Valider avec `get_errors` après chaque fichier modifié
- Ne corriger que les erreurs réelles (ignorer les warnings linter sur méthodes appelées depuis les templates)

---

### 4. ✅ Rédiger le done

Créer ou mettre à jour `tasks/done/done-<phase>[-suffixe].md` :
- Résumé des changements
- Pour chaque item : problème → cause racine (si bug) → solution → fichiers modifiés
- État du localStorage si modifié
- Mises à jour à apporter aux plans de tests

---

## 📌 Règles permanentes

| Règle | Raison |
|-------|--------|
| Spec avant code, toujours | Évite les allers-retours coûteux |
| Lire les done avant de coder | Évite de recréer ce qui existe déjà |
| Ne pas modifier les routes sans spec | Les routes sont contractuelles (SEO, liens externes) |
| Toujours `get_errors` après édition | Détecte les régressions immédiatement |
| Grouper les changements liés dans la même réponse | Réduit les états intermédiaires cassés |
| Ne jamais supprimer du code sans vérifier les usages | Risque de casser des composants silencieusement |
| Les `done.md` sont la source de vérité de l'état du projet | Relus à chaque nouvelle session |
| Une explication du développeur n'est pas un ordre d'action | Intégrer le contexte, ne pas agir sauf demande explicite |

---

## 🗂️ Convention de nommage

| Type | Pattern | Durée de vie | Emplacement |
|------|---------|--------------|-------------|
| Spec | `spec-<phase>-<feature>.md` | Éphémère | `tasks/specification/` |
| Done | `done-<phase>[-suffixe].md` | Éphémère | `tasks/done/` |
| Technique permanent | `<sujet>.md` ou `permanent-<sujet>.md` | ♾️ | `tasks/technical/` |
| Step / Phase | `phase<N>[-suffixe].md` | ♾️ | `steps/` |
| Vision | `vision.md` ou `vision[-sujet].md` | ♾️ | Racine `.ai_context/` |

---

## 💬 Communication avec le développeur

- **Si la demande est ambiguë** : poser UNE seule question précise, attendre la réponse
- **Si le développeur explique ou précise quelque chose** : mettre à jour le contexte mental uniquement — **ne pas déclencher d'action** sauf demande explicite
- **Si la tâche est longue** : annoncer le plan en bullet points et attendre un "go"
- **Si un bug est découvert en cours de route** : signaler, ne pas corriger sans accord (sauf s'il bloque la tâche en cours)
- **Si la spec doit évoluer** : mettre à jour le fichier spec ET noter la raison du changement
