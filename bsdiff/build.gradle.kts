plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("signing")
    id("maven-publish")
}

android {
    namespace = "io.github.junkfood02.bsdiff"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    ndkVersion = "26.3.11579264"

    publishing { singleVariant("release") { withSourcesJar() } }
}

kotlin { jvmToolchain(21) }

publishing {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("release") {
                groupId = "io.github.junkfood02.bsdiff"
                artifactId = "bsdiff"
                version = project.version.toString()

                afterEvaluate { from(components["release"]) }

                pom {
                    name.set("bsdiff")
                    description.set("bsdiff for Android")
                    url.set("https://github.com/junkfood02/bsdiff-android")
                    inceptionYear.set("2024")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("junkfood02")
                            name.set("junkfood02")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/junkfood02/bsdiff-android")
                        url.set("https://github.com/junkfood02/bsdiff-android")
                    }
                }
            }
        }

        repositories {
            maven { url = uri(layout.buildDirectory.dir("staging-deploy").get().asFile.path) }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["release"])
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
