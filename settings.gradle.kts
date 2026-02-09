pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PrivDNSToggle"
include(":app")

// Build output under root/builds/ with a subdirectory per module (e.g. builds/app/)
rootProject.layout.buildDirectory.set(rootProject.layout.projectDirectory.dir("builds"))
