package ai.documentationfirst.ddd.templates

import java.io.File
import java.time.Instant
import java.time.LocalDate

enum class AgentProfile(val label: String, val description: String) {
    STRICT(
        "Strict (Recommended)",
        "No terminal commands. Agent displays commands as code blocks only. No rename/delete."
    ),
    STANDARD(
        "Standard",
        "Build/install forbidden. Read-only commands (grep, find, cat) and tests allowed."
    ),
    PERMISSIVE(
        "Permissive",
        "All terminal commands allowed. Agent may rename/delete with caution."
    ),
}

object TemplateProvider {

    private fun today() = LocalDate.now().toString()
    private fun nowIso() = Instant.now().toString()

    // ── Static templates (permanent files) ────────────────────────────────────

    fun aiContextReadme(): String = """
        # `.ai_context` — Documentation-Driven Development v2

        Ce dossier est géré par le plugin **Documentation First** (JetBrains).
        Il structure la collaboration entre le développeur et l'agent IA tout au long du projet.

        ---

        ## Philosophie

        La méthode repose sur un principe simple : **la documentation n'est pas un livrable, c'est un outil de travail**.

        Le développeur et l'agent IA collaborent par **lecture et écriture de documents**.
        - Le développeur écrit ce qu'il veut faire, comprend ou décide.
        - L'agent lit, complète, affine, questionne, documente ce qu'il a fait.
        - Ensemble, ils maintiennent une base documentaire vivante qui sert de mémoire au projet.

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
        - Une nouvelle contrainte technique → un fichier dans `documents/technical/`
        - Un besoin fonctionnel précisé → un fichier dans `documents/specification/`
        - Une tâche terminée → un fichier de synthèse dans `documents/done/`

        ### Convention `permanent-`
        Un fichier dans `technical/` ou `specification/` préfixé par `permanent-` **ne sera pas effacé**
        lors du passage à un nouveau contexte.

        ### 3. Clore un contexte avant de commiter
        Un **contexte = une unité de travail = un commit Git**.
        Committez `.ai_context/` avant de passer à un nouveau contexte.

        ---

        ## Structure

        ```
        .ai_context/
        ├── README.md              ← ce fichier (permanent)
        ├── CONTRACT.md            ← règles pour l'agent (permanent)
        ├── CONTEXT.md             ← objectif et todo liste (contextuel)
        ├── context.json           ← métadonnées machine (contextuel)
        ├── history.log            ← journal des contextes passés (permanent)
        └── documents/
            ├── done/              ← synthèses de l'agent (contextuel)
            ├── specification/     ← specs fonctionnelles (permanent-* conservé)
            └── technical/         ← décisions techniques (permanent-* conservé)
        ```

        ---

        *Géré par [Documentation First Plugin](https://documentationfirst.ai) — MIT License*
    """.trimIndent()

    fun contractMd(profile: AgentProfile): String {
        val prohibitions = when (profile) {
            AgentProfile.STRICT -> """
                ## 🚫 PROHIBITIONS ABSOLUES

                > Ces règles s'appliquent **sans exception**.

                1. **Ne jamais utiliser d'outil d'exécution de commandes terminal.**
                   - Interdit : `npm install`, `npm run`, `ng build`, `git`, `node`, etc.
                   - Si une commande est nécessaire, l'afficher en bloc de code — le développeur l'exécute lui-même.
                2. **Ne jamais renommer ni supprimer de fichiers.**
                3. **Ne jamais modifier des fichiers hors du workspace.**
            """.trimIndent()

            AgentProfile.STANDARD -> """
                ## ⚠️ RESTRICTIONS

                1. **Ne jamais lancer de commandes build, install ou serve.**
                   - Interdit : `npm install`, `npm run build`, `ng build`, `ng serve`, `git push`, etc.
                   - Autorisé en lecture seule : `grep`, `find`, `cat`, `ls`
                   - Commandes de test (`ng test`, `npm test`) autorisées.
                2. **Ne jamais renommer ni supprimer de fichiers.**
                3. **Ne jamais modifier des fichiers hors du workspace.**
            """.trimIndent()

            AgentProfile.PERMISSIVE -> """
                ## ℹ️ RECOMMANDATIONS

                1. **Ne jamais modifier des fichiers hors du workspace.**
                2. **Afficher les commandes destructives** (delete, rename, publish) en bloc de code pour relecture.
                3. Les commandes build, install, test et serve peuvent être exécutées directement si nécessaire.
            """.trimIndent()
        }

        val permissions = when (profile) {
            AgentProfile.STRICT -> """
                ## ✅ Ce que l'agent est autorisé à faire

                | Action | Autorisé | Notes |
                |---|---|---|
                | Modifier des fichiers existants | ✅ Oui | Sans confirmation préalable |
                | Créer de nouveaux fichiers techniques | ✅ Oui | |
                | Lire des fichiers du projet | ✅ Oui | |
                | Rechercher dans le code | ✅ Oui | Lecture seule |
                | Mettre à jour `.ai_context/` | ✅ Oui | |
                | Exécuter des commandes terminal | ❌ Non | Afficher en bloc de code uniquement |
                | Renommer ou supprimer des fichiers | ❌ Non | |
            """.trimIndent()

            AgentProfile.STANDARD -> """
                ## ✅ Ce que l'agent est autorisé à faire

                | Action | Autorisé | Notes |
                |---|---|---|
                | Modifier des fichiers existants | ✅ Oui | |
                | Créer de nouveaux fichiers | ✅ Oui | |
                | Lire des fichiers du projet | ✅ Oui | |
                | Commandes lecture seule (`grep`, `find`, `cat`) | ✅ Oui | |
                | Lancer les tests (`npm test`, `ng test`) | ✅ Oui | |
                | Commandes build / install / serve | ❌ Non | Afficher en bloc de code |
                | Renommer ou supprimer des fichiers | ❌ Non | |
            """.trimIndent()

            AgentProfile.PERMISSIVE -> """
                ## ✅ Ce que l'agent est autorisé à faire

                | Action | Autorisé | Notes |
                |---|---|---|
                | Modifier / créer des fichiers | ✅ Oui | |
                | Lire des fichiers | ✅ Oui | |
                | Toutes commandes terminal | ✅ Oui | |
                | Renommer / supprimer | ✅ Oui | Avec prudence |
                | Modifier des fichiers hors workspace | ❌ Non | |
            """.trimIndent()
        }

        return """
            # AI Agent — Contrat d'interaction

            *Profil : **${profile.name.lowercase()}***

            Ce fichier définit les règles d'interaction entre le développeur et l'agent IA sur ce projet.
            **L'agent doit lire et respecter ce contrat avant toute action.**

            ---

            $prohibitions

            ---

            $permissions

            ---

            ## 🧠 Préférences de communication

            - Répondre en **français**
            - Être **concis** : pas de répétition du code existant dans les explications
            - Signaler les **erreurs préexistantes** séparément des erreurs introduites
            - Ne pas demander de confirmation pour des changements évidents — **agir directement**
            - En cas de doute sur le périmètre, **poser une seule question ciblée**

            ---

            ## 📁 Documentation de référence

            Tout le contexte est centralisé dans [`.ai_context/`](./.ai_context/).
            **Lire et suivre les directives de ces fichiers** avant toute modification.
        """.trimIndent()
    }

    // ── Contextual templates ───────────────────────────────────────────────────

    fun contextMd(title: String, description: String, todos: List<String>): String {
        val todoLines = if (todos.isEmpty()) "- [ ] ..." else todos.joinToString("\n") { "- [ ] $it" }
        return """
            # Contexte — $title

            *Démarré : ${today()}*

            ---

            ## Description

            $description

            ---

            ## Todo

            $todoLines
        """.trimIndent()
    }

    fun contextJson(title: String, description: String): String {
        val escaped = { s: String -> s.replace("\"", "\\\"") }
        return """{"title":"${escaped(title)}","description":"${escaped(description)}","startedAt":"${nowIso()}"}"""
    }

    fun projectReadme(projectName: String): String = """
        # For AI Agent :

        Read all [context](./.ai_context) for context and needs.
        L'agent doit appliquer les conditions spécifiées par le fichier CONTRACT.md
        Le contexte du développement actuel est présenté dans CONTEXT.md et tous les fichiers du répertoire `documents/`.
        Le sous-répertoire `done/` contient des fichiers MD rédigés par l'agent expliquant ce qui a été fait dans ce contexte.
        Le sous-répertoire `technical/` contient des bonnes pratiques et conseils techniques.
        Le sous-répertoire `specification/` contient des détails fonctionnels et d'architecture.

        ---

        # $projectName

        > *(Décrivez votre projet ici)*
    """.trimIndent()

    // ── Scaffold helpers ───────────────────────────────────────────────────────

    fun scaffoldInit(
        aiContextRoot: File,
        profile: AgentProfile,
        title: String,
        description: String,
        todos: List<String>
    ) {
        aiContextRoot.mkdirs()
        for (sub in listOf("done", "specification", "technical")) {
            File(aiContextRoot, "documents/$sub").mkdirs()
            File(aiContextRoot, "documents/$sub/.gitkeep").also {
                if (!it.exists()) it.writeText("")
            }
        }
        File(aiContextRoot, "README.md").also { if (!it.exists()) it.writeText(aiContextReadme()) }
        File(aiContextRoot, "CONTRACT.md").also { if (!it.exists()) it.writeText(contractMd(profile)) }
        File(aiContextRoot, "CONTEXT.md").writeText(contextMd(title, description, todos))
        File(aiContextRoot, "context.json").writeText(contextJson(title, description))

        // Create or update project README.md at root
        val projectRoot = aiContextRoot.parentFile
        val projectReadme = File(projectRoot, "README.md")
        if (!projectReadme.exists()) {
            projectReadme.writeText(projectReadme(projectRoot.name))
        } else if (!projectReadme.readText().contains(".ai_context")) {
            val agentHeader = """
                # For AI Agent :

                Read all [context](./.ai_context) for context and needs.
                L'agent doit appliquer les conditions spécifiées par le fichier CONTRACT.md
                Le contexte du développement actuel est présenté dans CONTEXT.md et tous les fichiers du répertoire `documents/`.

                ---

            """.trimIndent()
            projectReadme.writeText(agentHeader + "\n" + projectReadme.readText())
        }
    }

    fun scaffoldNewContext(
        aiContextRoot: File,
        title: String,
        description: String,
        todos: List<String>
    ) {
        // Archive old context into history.log
        val contextJsonFile = File(aiContextRoot, "context.json")
        if (contextJsonFile.exists()) {
            val oldJson = contextJsonFile.readText().trim().trimEnd('}')
            val historyLine = """$oldJson,"endedAt":"${nowIso()}"}"""
            File(aiContextRoot, "history.log").appendText(historyLine + "\n")
        }

        // Clear done/ entirely
        File(aiContextRoot, "documents/done").listFiles()?.forEach { it.delete() }

        // Clear specification/ and technical/ except permanent-*
        for (sub in listOf("specification", "technical")) {
            File(aiContextRoot, "documents/$sub").listFiles()
                ?.filter { !it.name.startsWith("permanent-") && it.name != ".gitkeep" }
                ?.forEach { it.delete() }
        }

        // Write new contextual files
        File(aiContextRoot, "CONTEXT.md").writeText(contextMd(title, description, todos))
        File(aiContextRoot, "context.json").writeText(contextJson(title, description))
    }
}
