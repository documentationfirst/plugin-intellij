# AI Agent — Contrat d'interaction

Ce fichier définit les règles d'interaction entre le développeur et l'agent IA sur ce projet.
**L'agent doit lire et respecter ce contrat avant toute action.**

---

## ✅ Ce que l'agent est autorisé à faire

| Action | Autorisé | Notes |
|---|---|---|
| Modifier des fichiers existants (`.ts`, `.html`, `.css`, `.json`, `.md`) | ✅ Oui | Sans demander confirmation préalable |
| Créer de nouveaux fichiers techniques | ✅ Oui | Composants, services, specs, configs, docs |
| Lire des fichiers du projet | ✅ Oui | Pour analyser avant d'agir |
| Rechercher dans le code (`grep`, `find`) | ✅ Oui | |
| Proposer et expliquer des changements | ✅ Oui | |
| Mettre à jour la documentation `ai_md_files/` | ✅ Oui | |

---

## ❌ Ce que l'agent ne doit PAS faire

| Action | Interdit | Raison |
|---|---|---|
| Exécuter des commandes terminal (`npm install`, `git`, `ng`, etc.) | ❌ Non | Le développeur les opère lui-même |
| Lancer des builds ou des tests | ❌ Non | Idem |
| Modifier des fichiers hors du workspace | ❌ Non | |
| Renommer ou supprimer des fichiers | ❌ Non | Action trop destructive sans contrôle humain |

> **Rappel** : si une commande terminal est nécessaire, l'agent doit **l'afficher en clair** pour que le développeur l'exécute lui-même, sans utiliser l'outil d'exécution.

---

## 🧠 Préférences de communication

- Répondre en **français**
- Être **concis** : pas de répétition du code existant dans les explications
- Signaler les **erreurs préexistantes** séparément des erreurs introduites par les modifications
- Ne pas demander de confirmation pour des changements évidents — **agir directement**
- En cas de doute sur le périmètre, **poser une seule question ciblée** plutôt que plusieurs

---

## Context, Documentation, Recommendations, and Best Practices

All context, technical documentation, migration recommendations, and best practices are centralized in the [`./.ai_context/`](./.ai_context/) directory.

**Please read and follow the directives in these files** before making changes or starting new developments.
