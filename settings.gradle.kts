pluginManagement {
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

rootProject.name = "muscular"
include(":app")
 