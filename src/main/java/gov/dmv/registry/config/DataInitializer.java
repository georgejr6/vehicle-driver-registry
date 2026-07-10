package gov.dmv.registry.config;

import gov.dmv.registry.model.VehicleType;
import gov.dmv.registry.service.RegistryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds a little demo data at startup so the portal isn't empty the first time
 * you open it.
 *
 * HOW IT RUNS: a {@code @Component} that implements {@link CommandLineRunner} is
 * something Spring runs ONCE, automatically, right after the application starts.
 * It is the standard place to put "on startup, do this" logic.
 *
 * Because our database is in-memory (see application.properties), it starts
 * empty every run, so this seed data reappears fresh each time you launch. If
 * you switch to a file-based database, guard this with a "only if empty" check.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RegistryService registry;

    public DataInitializer(RegistryService registry) {
        this.registry = registry;
    }

    @Override
    public void run(String... args) {
        // Only seed if the registry is empty, so we don't pile up duplicates.
        if (!registry.listPeople().isEmpty()) {
            return;
        }

        var ada = registry.registerPerson("Ada", "Lovelace", LocalDate.of(1990, 12, 10));
        var alan = registry.registerPerson("Alan", "Turing", LocalDate.of(1985, 6, 23));
        registry.registerPerson("Grace", "Hopper", LocalDate.of(2010, 4, 1)); // a minor, for demoing the age rule

        registry.registerVehicle(ada.getId(), "1HGBH41JXMN109186",
                "Toyota", "Corolla", 2020, VehicleType.CAR);
        registry.registerVehicle(alan.getId(), "JH4KA8260MC000000",
                "Honda", "CB500", 2019, VehicleType.MOTORCYCLE);

        registry.issueLicense(ada.getId());
    }
}
