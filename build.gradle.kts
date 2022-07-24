import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.2"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}
configurations {
    all {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

dependencies {
    implementation("com.redislabs:spark-redis_2.12:3.1.0")
    implementation("org.apache.spark:spark-core_2.12:3.3.0")
    implementation("org.apache.spark:spark-sql_2.12:3.3.0")
    implementation("org.jetbrains.kotlinx.spark:kotlin-spark-api-3.2:1.1.0")
    implementation("org.codehaus.janino:janino:3.0.16") // keep 3.0.x for compatibility
    implementation("org.codehaus.janino:commons-compiler:3.0.16") // keep 3.0.16 for compatibility
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")

    implementation("org.apache.hadoop:hadoop-client-api:3.3.3")
    implementation("com.google.protobuf:protobuf-java:3.21.2")
    implementation("com.google.guava:guava:31.1-jre")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
