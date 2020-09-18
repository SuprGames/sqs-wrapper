import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0"
    id("io.spring.dependency-management") version ("1.0.9.RELEASE")
    `maven-publish`
}

group = "io.suprgames"

description = "Sqs-Wrapper"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:2.13.18")
    }
}

dependencies {
    //We want to provide the AWS SQS library here
    api("software.amazon.awssdk:sqs")
    implementation("io.suprgames:kjson-mapper:v0.1.0")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.1")
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.2.0")
    implementation("org.slf4j:slf4j-simple:1.7.30")
}


tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            groupId = "io.suprgames"
            artifactId = "sqs-wrapper"
            if (!System.getenv("NEW_VERSION").isNullOrBlank()) {
                version = System.getenv("NEW_VERSION")
            }
            from(components["kotlin"])

        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/suprgames/sqs-wrapper")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
