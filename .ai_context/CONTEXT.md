# Modification de l'input et de l'output du plug in

Le répertoire d'output sera maintenant nommé ".ai_context" au lieu de "ai_md_files"

Le README_AI.md n'existe plus. A la place un paragraphe est ajouté en tête du README (voir exemple de ce projet) pour déclarer à l'agent la présence du dossier ai_context.
un CONTRACT.md contiendra le reste du README_AI, c'est-à-dire la partie des informations sur le profil du développeur par rapport à l'agent.

En input, le plug-in va maintenant demander un contexte : un titre, une description, une todo liste.
Ce contexte sera ensuite enregistré dans CONTEXT.md, et le context.json contiendra titre, description, date de début, dans l'objectif de logger ces infos avec date de fin lors de la demande d'un nouveau contexte.

Un contexte est aussi une suite de documents "documents/*" dont 3 sous-répertoires "done", "specification", "technical", que le développeur devra compléter au besoin.

Lors du passage à un nouveau contexte, l'ancien est zippé (sans le CONTRACT.md), avec pour nom "context12-12-2012.zip" par exemple, et laissé dans .ai_context , le nouveau garde l'ancien CONTRACT.md, mais réinitialise CONTEXT.md et context.json avec le nouveau contexte, et les trois sous-répertoires sont vidés.
Et un fichier contexts.logs doit etre écrits.

Les boutons de la fenetre plugin changent. Maintenant on demandera l'initialisation du contexte, ou le passage à un nouveau. En-dessous sera présenté l'arborescence du contexte, et les nouveaux fichiers dans les sous-répertoires pourront etre créé par un clic droit.

Et en-dessous de l'arborescence sera maintenant une nouvelle partie présentant le contexte : date, titre, description, todo liste cliquable (et mettant à jour le fichier CONTEXT.md)

# Contexte — Refonte architecture .ai_context (JetBrains plugin)

*Démarré : 2026-04-20*

---

## Description

Refonte complète du plugin JetBrains Documentation First :
- Remplacement de `ai_md_files/` par `.ai_context/`
- Nouveau modèle de contexte (titre, description, todo liste, documents)
- Nouvelle UI du tool window (arborescence + panneau contexte interactif)
- Convention `permanent-` pour les fichiers persistants entre contextes
- Archivage délégué à Git (pas de zip)
- Tous les tests mis à jour

---

## Todo

- [x] Renommer `ai_md_files/` → `.ai_context/`
- [x] Supprimer `README_AI.md` → remplacer par `CONTRACT.md` dans `.ai_context/`
- [x] Ajouter paragraphe agent en tête du README principal
- [x] Implémenter `scaffoldInit` (profil + titre + description + todos)
- [x] Implémenter `scaffoldNewContext` (history.log + nettoyage + permanent-*)
- [x] Implémenter `contextMd` et `contextJson`
- [x] Générer `README.md` dans `.ai_context/` à l'init
- [x] Nouvelle UI : bouton "Initialiser" / "Nouveau contexte"
- [x] Arborescence `documents/` dans le tool window
- [x] Panneau contexte en bas (titre, description, date, todos cliquables)
- [x] Convention `permanent-` : gras bleu dans l'arborescence
- [x] Fichiers `done/` en gris dans l'arborescence
- [x] Clic droit sur dossier → "Nouveau fichier .md"
- [x] Warning Git avant passage à un nouveau contexte
- [x] Mettre à jour tous les tests (TemplateProviderTest, AgentComplianceTest, DddDetectorTest)
- [x] Rédiger spécification `context-architecture.md`
- [ ] Porter ces changements sur le plugin VSCode
