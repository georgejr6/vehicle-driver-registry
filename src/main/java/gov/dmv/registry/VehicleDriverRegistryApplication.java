package gov.dmv.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The STARTING POINT of the whole web application.
 *
 * In IntelliJ, click the green ▶ next to main() and choose "Run". Spring Boot
 * then:
 *   1. scans this package (gov.dmv.registry) and everything under it,
 *   2. finds our components (controllers, services, repositories),
 *   3. wires them together automatically,
 *   4. starts an embedded Tomcat web server (default http://localhost:8080).
 *
 * The one magic annotation below does most of that:
 *
 *   @SpringBootApplication is actually THREE annotations rolled into one:
 *     - @Configuration       : this class can define app settings/beans
 *     - @EnableAutoConfiguration : "look at the libraries on the classpath and
 *                                   configure sensible defaults" (e.g. because
 *                                   H2 + JPA are present, set up a database)
 *     - @ComponentScan       : "find my @Service/@Controller/@Repository
 *                               classes automatically"
 *
 * IMPORTANT: this class lives in the TOP package (gov.dmv.registry). Component
 * scanning starts here and searches downward, which is why every other class we
 * write goes in a sub-package like .model, .service, .web, etc.
 */
@SpringBootApplication
public class VehicleDriverRegistryApplication {

    public static void main(String[] args) {
        // This single call boots the entire application and blocks here,
        // keeping the web server running until you stop it.
        SpringApplication.run(VehicleDriverRegistryApplication.class, args);
    }
}
