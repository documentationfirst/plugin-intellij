plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "ai.documentationfirst"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        pluginVerifier()
        zipSigner()
        instrumentationTools()
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.10")
}

intellijPlatform {
    pluginConfiguration {
        name = "DDD2 — Documentation-Driven Development v2"
        version = "1.0.0"
        description = """
            Make Documentation-Driven Development a first-class AI citizen of your IDE.
            Auto-detects .ai_context/, scaffolds templates per stack, and provides
            a DDD tool window to manage all your context files.
        """.trimIndent()

        ideaVersion {
            sinceBuild = "243"
            untilBuild = provider { null }
        }

        vendor {
            name = "Documentation First"
            url = "https://documentationfirst.ai"
            email = "contact@documentationfirst.ai"
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

tasks.test {
    useJUnitPlatform()
}

