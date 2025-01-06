pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add Clover's Maven repository
        maven(url = uri("https://s3.amazonaws.com/sdk-release/"))
        maven {
            setUrl("https://jitpack.io")
        }
    }
}

rootProject.name = "MSAdsApp"
include(":app")
