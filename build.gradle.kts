plugins {
    id("com.android.application") version "9.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}

// Each subproject builds into builds/<moduleName>/ (e.g. builds/app/)
subprojects {
    layout.buildDirectory.set(rootProject.layout.buildDirectory.dir(project.name))
}
