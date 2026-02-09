plugins {
    id("com.android.application") version "9.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}

// Build output under root/builds/ with a subdirectory per module (e.g. builds/app/)
layout.buildDirectory.set(layout.projectDirectory.dir("builds"))
subprojects {
    layout.buildDirectory.set(rootProject.layout.buildDirectory.dir(project.name))
}
