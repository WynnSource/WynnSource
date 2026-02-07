plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.axion.release)
    `maven-publish`
}

scmVersion {
    tag {
        prefix.set("")
        versionSeparator.set("")
        initialVersion { _, _ -> "0.1.0" }
    }
    checks {
        uncommittedChanges.set(false)
        aheadOfRemote.set(false)
    }
}

version = scmVersion.version
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven {
        name = "DevAuth"
        url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    }

    maven {
        name = "Terraformers"
        url = uri("https://maven.terraformersmc.com/")
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven {
        name = "OwOLib"
        url = uri("https://maven.wispforest.io/releases/")
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "JitPack"
                url = uri("https://jitpack.io")
            }
        }
        filter {
            includeModule("com.github.kdl-org", "kdl4j") // kdl4j for owolib
        }
    }
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("wynnsource") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

dependencies {
    // Minecraft & Mappings
    minecraft(libs.minecraft)
    mappings("${libs.yarn.mappings.get()}:v2")

    // Fabric
    modImplementation(libs.bundles.fabric)

    // ModMenu integration
    modImplementation(libs.modMenu)
    // Wynntils
    modImplementation(libs.wynntils)
    // OwOLib for GUI
    modImplementation(libs.owoLib)
    include(libs.owoLibSentinal)

    // QoL mods for development
    modLocalRuntime(libs.devAuth)
    modLocalRuntime(libs.resourcePackCached)
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
    baseline = file("$projectDir/config/detekt/baseline.xml")
    parallel = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = "21"
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}
