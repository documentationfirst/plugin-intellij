# Done — Parité IntelliJ / VSCode pour le dossier `skills/`

*Complété : 2026-04-30*

---

## Contexte

Après implémentation complète de `skills/` dans le plugin VSCode (icône dédiée, `contextValue` distinct,
commande `newSkill`), le plugin IntelliJ avait deux lacunes de parité.

---

## Ce qui a été fait

### 1. `DddActions.kt` — ajout de `NewSkillAction`

Nouvelle action dédiée à la création d'un fichier skill dans `skills/` :
- Placeholder rappelant la convention `permanent-`
- Template généré avec sections `Rôle` et `Règles`
- Icône `AllIcons.Nodes.Editorconfig` (distincte de `AllIcons.FileTypes.Text`)

```kotlin
class NewSkillAction(private val skillsDir: File, private val project: Project) :
    AnAction("Nouveau skill .md", null, AllIcons.Nodes.Editorconfig) {
    override fun actionPerformed(e: AnActionEvent) {
        val name = askInput(project, "Nom du skill", "ex: permanent-dev-typescript") ?: return
        val file = File(skillsDir, "$name.md")
        if (!file.exists()) {
            file.writeText("# Skill — ${name.removePrefix("permanent-")}\n\n...\n\n## Rôle\n\n## Règles\n\n")
        }
        // refresh + open...
    }
}
```

---

### 2. `DddToolWindowFactory.kt` — menu clic droit différencié

Le menu contextuel distingue maintenant `skills/` des autres dossiers :

```kotlin
// Avant : "New file .md" pour TOUS les dossiers
// Après :
val isSkills = file.name == "skills"
if (isSkills) {
    menu.add(JMenuItem("New skill .md") { NewSkillAction(file, project) })
} else {
    menu.add(JMenuItem("New file .md") { NewDocumentAction(file, project) })
}
```

---

### 3. `DddTreeCellRenderer` — icône distincte pour `skills/`

```kotlin
// Avant
file.isDirectory -> AllIcons.Nodes.Folder

// Après
file.isDirectory && file.name == "skills" -> AllIcons.Nodes.Editorconfig
file.isDirectory -> AllIcons.Nodes.Folder
```

---

## Parité plugins

| Élément | VSCode | IntelliJ |
|---|---|---|
| Icône `skills/` | `mortar-board` | `Editorconfig` |
| Action dédiée | `ddd.newSkill` | `NewSkillAction` |
| Menu contextuel | inline `dddSkillsFolder` | clic droit "New skill .md" |
| Template skill | `Rôle` + `Règles` | `Rôle` + `Règles` |
| Convention `permanent-` | hint dans le placeholder | hint dans le placeholder |

