plugins {
	java
	id("org.springframework.boot") version "3.3.3"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "app.clutter-map"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter") // Spring Boot core dependencies and auto-configuration.
	implementation("org.springframework.boot:spring-boot-starter-web") // Spring MVC for building RESTful web services.

	implementation("com.google.oauth-client:google-oauth-client:1.32.1") // OAuth 2.0 client support for Google services.
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.32.1") // For HTTP transport
    implementation("com.google.api-client:google-api-client-gson:1.32.1") // For JSON factory, JSON serialization/deserialization support for Google API clients.

	implementation ("org.springframework.boot:spring-boot-starter-security") // Spring Security for authentication and authorization.
	implementation ("org.springframework.boot:spring-boot-starter-oauth2-client") // OAuth 2.0 client support for third-party login integrations.
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server") // OAuth 2.0 resource server support for verifying JWT tokens.

    implementation("io.jsonwebtoken:jjwt-impl:0.11.5") // JJWT core implementation for handling JWT tokens.
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5") // Jackson JSON parser support for JJWT.
	implementation("io.jsonwebtoken:jjwt-api:0.11.5") // JJWT API for creating and validating JWT tokens.

	implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Spring Data JPA for database access and ORM functionality.
	implementation("org.postgresql:postgresql:42.2.23") // PostgreSQL JDBC driver for connecting to a PostgreSQL database.
	testImplementation("org.springframework.boot:spring-boot-starter-test") // Spring Boot test utilities, including JUnit and Mockito.
	testImplementation("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher") // JUnit platform launcher for running tests.

	implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")

	implementation("org.springframework.boot:spring-boot-starter-validation")

	// IN CASE YOU GET AN ERROR AT THE TOP ABOUT THE GRADLE PLUGIN
	// https://stackoverflow.com/questions/68321708/could-not-run-phased-build-action-using-connection-to-gradle-distribution
}

tasks.withType<Test> {
	useJUnitPlatform()
}
