package com.pink.pfa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.services.WebScraperService;


/**
 * PfaApplication<br>
 * <br>
 * Entry point for the Pink Fairy Armadillos (PFA) backend application.
 * <p>
 * This class bootstraps the Spring Boot application by:
 * <ul>
 *   <li>Enabling component scanning for the base package ({@code com.pink.pfa}) and its subpackages.</li>
 *   <li>Activating Spring Boot auto-configuration for web, security, JPA, and other configured modules.</li>
 *   <li>Starting the embedded servlet container (e.g., Tomcat) for handling HTTP requests.</li>
 * </ul>
 *
 * The {@link SpringBootApplication} annotation is a convenience annotation that combines:
 * <ul>
 *   <li>{@code @Configuration}</li>
 *   <li>{@code @EnableAutoConfiguration}</li>
 *   <li>{@code @ComponentScan}</li>
 * </ul>
 *
 * When executed, this class initializes the full Spring application context
 * and begins listening for incoming API requests.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class PfaApplication {
	/**
	 * Main method used to launch the Spring Boot application.
	 * Delegates to {@link SpringApplication#run(Class, String...)} which
	 * initializes the Spring context and embedded web server.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(PfaApplication.class, args);
	}
}
