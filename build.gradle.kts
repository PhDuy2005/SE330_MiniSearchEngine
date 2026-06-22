plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.NgonNguLapTrinhJava"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

val vnCoreNlpRaw by configurations.creating {
	isTransitive = false
}

val cleanVnCoreNlpJar by tasks.registering(Jar::class) {
	from({ zipTree(vnCoreNlpRaw.singleFile) }) {
		exclude("org/slf4j/**")
	}

	archiveBaseName.set("VnCoreNLP-clean")
	archiveVersion.set("1.1.1")
	destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("com.fasterxml.jackson.core:jackson-databind")

	implementation("org.apache.lucene:lucene-core:9.10.0")

	implementation("org.apache.lucene:lucene-queryparser:9.10.0")

	implementation("org.apache.lucene:lucene-analysis-common:9.10.0")

	vnCoreNlpRaw("com.github.vncorenlp:VnCoreNLP:1.1.1")
	implementation(files(cleanVnCoreNlpJar.map { it.archiveFile }))

	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.security:spring-security-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("compileJava") {
	dependsOn(cleanVnCoreNlpJar)
}

tasks.named("bootRun") {
	dependsOn(cleanVnCoreNlpJar)
}

tasks.named("bootJar") {
	dependsOn(cleanVnCoreNlpJar)
}
