# Contexte — Migration de la structure `.ai_context` vers v2

*Démarré : 2026-05-15*

---

## Description

Refonte de la structure `.ai_context/` pour les deux plugins (VSCode et IntelliJ)
afin d'aligner sur la nouvelle arborescence de référence définie dans `skills/methodology-ddd-ai.md` :
- `documents/` → `tasks/`
- `history.log` → `history.json`
- Ajout de `vision.md` (permanent, créé à l'init)
- Ajout de `steps/` (jalons/features, permanent)
- Enrichissement du flow `init` : vision → context → steps → tasks

---

## Todo

- [x] Mettre à jour `TemplateGenerator.ts` (VSCode) : `tasks/`, `steps/`, `vision.md`, nouveaux params
- [x] Mettre à jour `initContext.ts` : flow vision + steps + todos
- [x] Mettre à jour `newContext.ts` : message d'avertissement
- [x] Mettre à jour `DddTreeProvider.ts` : afficher `tasks/`, `steps/`, `skills/`
- [x] Mettre à jour `TemplateProvider.kt` (IntelliJ) : mêmes changements
- [x] Mettre à jour `DddActions.kt` (IntelliJ) : flow init avec vision + steps
- [x] Mettre à jour `DddToolWindowFactory.kt` (IntelliJ) : arborescence + icônes
- [x] Mettre à jour `README.md` des deux `.ai_context/`
- [x] Créer `vision.md` dans les deux `.ai_context/`
- [ ] Créer `steps/` et `tasks/` dans les deux `.ai_context/` (commandes terminal ci-dessous)
- [ ] Migrer le contenu de `documents/` vers `tasks/` dans les deux `.ai_context/`
- [ ] Supprimer les anciens dossiers `documents/` après migration

## Commandes à exécuter (WSL)

```bash
# Plugin IntelliJ
cd ~/CLAUDE/documentationfirst/plugin-intellij/.ai_context
mkdir -p steps tasks/done tasks/specification tasks/technical
touch steps/.gitkeep tasks/done/.gitkeep tasks/specification/.gitkeep tasks/technical/.gitkeep
cp documents/specification/*.md tasks/specification/
cp documents/done/*.md tasks/done/ 2>/dev/null || true
cp documents/technical/*.md tasks/technical/ 2>/dev/null || true
rm -rf documents/

# Plugin VSCode
cd ~/CLAUDE/documentationfirst/plugin-vscode/.ai_context
mkdir -p steps tasks/done tasks/specification tasks/technical
touch steps/.gitkeep tasks/done/.gitkeep tasks/specification/.gitkeep tasks/technical/.gitkeep
cp documents/specification/*.md tasks/specification/
cp documents/done/*.md tasks/done/ 2>/dev/null || true
cp documents/technical/*.md tasks/technical/ 2>/dev/null || true
rm -rf documents/
```
