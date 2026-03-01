import build.buf.gradle.BUF_GENERATE_TASK_NAME
import build.buf.gradle.GENERATED_DIR
import build.buf.gradle.GenerateTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.axion.release)
    alias(libs.plugins.buf)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.shadow)
    `maven-publish`
}

scmVersion {
    tag {
        prefix.set("v")
        versionSeparator.set("")
        initialVersion { _, _ -> "0.1.0" }
    }
    versionCreator { version, position ->
        val branchName = position.branch?.split("/")?.getOrNull(1) ?: "unknown"
        "$branchName-$version"
    }
    snapshotCreator { _, position ->
        val isDirty = !position.isClean
        val revision = "-pre-${position.shortRevision}"

        if (isDirty) {
            "$revision-UNCOMMITTED"
        } else {
            revision
        }
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

    // Protobuf
    shadow(libs.bundles.protobuf)
    implementation(libs.bundles.protobuf)

    // Ktor
    shadow(libs.bundles.ktor)
    implementation(libs.bundles.ktor)

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
    dependsOn(tasks.named(BUF_GENERATE_TASK_NAME))
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }

    dependsOn(tasks.named(BUF_GENERATE_TASK_NAME))
    dependsOn(tasks.named("openApiGenerate"))

    sourceSets.main {
        kotlin.srcDir(layout.buildDirectory.dir("bufbuild/$GENERATED_DIR/kotlin"))
        kotlin.srcDir(layout.buildDirectory.dir("openapi/src/main/kotlin"))
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    sourceSets.main {
        java.srcDir(layout.buildDirectory.dir("bufbuild/$GENERATED_DIR/java"))
    }
}

tasks.jar {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

tasks.named<Jar>("sourcesJar") {
    // Prevent Gradle from erroring implicit use
    dependsOn(BUF_GENERATE_TASK_NAME)
    dependsOn(tasks.named("openApiGenerate"))
}

// Buf
tasks.named<GenerateTask>(BUF_GENERATE_TASK_NAME) {
    // Prevent Gradle from erroring implicit use
    // no idea why buf use this
    dependsOn(tasks.named("downloadAssets"))
}

buf {
    enforceFormat = false
    configFileLocation = rootProject.file("schema/buf.yaml")
    generate {
        includeImports = true
        templateFileLocation = rootProject.file("buf.gen.yaml")
    }
}

tasks.named("bufLint") {
    enabled = false
}

// OpenAPI Generator
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec = "$projectDir/src/main/resources/wynnsource-api.yaml"
    outputDir = layout.buildDirectory.dir("openapi").get().asFile.absolutePath

    configOptions.set(
        mapOf(
            "useCoroutines" to "true",
            "library" to "jvm-ktor",
            "serializationLibrary" to "kotlinx_serialization",
            "enumPropertyNaming" to "original",
            "packageName" to "fyw.fyi.wynnsource.server",
            "apiPackage" to "fyw.fyi.wynnsource.server.api",
            "modelPackage" to "fyw.fyi.wynnsource.server.model",
        )
    )
    typeMappings.set(
        mapOf(
            "AnyType" to "JsonElement",
            "java.time.OffsetDateTime" to "kotlin.time.Instant",
            "Items" to "JsonArray",
        )
    )
    importMappings.set(
        mapOf(
            "JsonElement" to "kotlinx.serialization.json.JsonElement",
            "JsonArray" to "kotlinx.serialization.json.JsonArray",
        )
    )
}

// Shadow Jar
tasks.withType<ShadowJar> {
    archiveClassifier.set("shadow")
    mergeServiceFiles()

    from(sourceSets.main.get().output)
    sourceSets.findByName("client")?.let { from(it.output) }

    configurations = listOf(project.configurations.shadow.get())
    dependencies {
        include(dependency("io.ktor:.*:.*"))
        include(dependency("com.google.protobuf:.*:.*"))
    }

    // Ktor
    relocate("io.ktor", "fyw.fyi.wynnsource.libs.io.ktor")

    // Protobuf
    relocate("com.google.protobuf", "fyw.fyi.wynnsource.libs.com.google.protobuf")

    exclude("**/*.proto") // Exclude .proto wellknown types, which are not needed at runtime
    exclude("java/core/**") // Protobuf's Java core library, which is not needed at runtime
    exclude("src/**") // Google why!!
    exclude("google/protobuf/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/scripts/**")
    exclude("META-INF/*kotlin_module") // No need for them in runtime

    dependsOn(tasks.named("jar"))
}

tasks.withType<RemapJarTask> {
    val shadowJar = tasks.named<ShadowJar>("shadowJar")
    dependsOn(shadowJar)
    mustRunAfter(shadowJar)
    inputFile = shadowJar.get().archiveFile
}
