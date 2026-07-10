package gov.dmv.registry.service;

import gov.dmv.registry.model.DriverLicense;
import gov.dmv.registry.model.Person;
import gov.dmv.registry.model.Vehicle;
import gov.dmv.registry.model.VehicleType;
import gov.dmv.registry.repository.DriverLicenseRepository;
import gov.dmv.registry.repository.PersonRepository;
import gov.dmv.registry.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for RegistryService, now running against a REAL (in-memory H2) database.
 *
 * @DataJpaTest is a Spring "test slice": it starts JUST the database + JPA
 * repositories (not the whole web app), which is fast. It also wraps each test
 * in a transaction that is ROLLED BACK afterwards, so every test starts with a
 * clean, empty database and tests never affect one another.
 *
 * Spring injects the real repositories with @Autowired; we then build the
 * service on top of them - exactly how the app wires things, but in miniature.
 */
@DataJpaTest
class RegistryServiceTest {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DriverLicenseRepository licenseRepository;

    private RegistryService registry;

    @BeforeEach
    void setUp() {
        registry = new RegistryService(personRepository, vehicleRepository, licenseRepository);
    }

    @Test
    @DisplayName("Registering a person stores them and the database assigns an id")
    void registersPerson() {
        Person p = registry.registerPerson("Grace", "Hopper", LocalDate.of(1980, 1, 1));

        assertEquals("Grace Hopper", p.getFullName());
        assertTrue(p.getId() != null, "database should have assigned an id");
        assertEquals(1, registry.listPeople().size());
    }

    @Test
    @DisplayName("Registering a vehicle to an unknown owner is rejected")
    void rejectsVehicleForUnknownOwner() {
        assertThrows(RegistryException.class, () ->
                registry.registerVehicle(999L, "VIN123", "Ford", "F-150", 2021, VehicleType.TRUCK));
    }

    @Test
    @DisplayName("Two vehicles cannot share the same VIN")
    void rejectsDuplicateVin() {
        Person owner = registry.registerPerson("Katherine", "Johnson", LocalDate.of(1975, 8, 26));
        registry.registerVehicle(owner.getId(), "SAMEVIN", "Tesla", "Model 3", 2022, VehicleType.CAR);

        assertThrows(RegistryException.class, () ->
                registry.registerVehicle(owner.getId(), "SAMEVIN", "Kia", "Niro", 2023, VehicleType.CAR));
    }

    @Test
    @DisplayName("Transferring a vehicle changes its owner")
    void transfersVehicleOwnership() {
        Person seller = registry.registerPerson("Seller", "One", LocalDate.of(1970, 3, 3));
        Person buyer = registry.registerPerson("Buyer", "Two", LocalDate.of(1972, 4, 4));
        Vehicle v = registry.registerVehicle(seller.getId(), "TRANSVIN", "Mazda", "3", 2018, VehicleType.CAR);

        Vehicle moved = registry.transferVehicle(v.getId(), buyer.getId());

        assertEquals(buyer.getId(), moved.getOwner().getId());
        assertEquals(0, registry.listVehiclesOwnedBy(seller.getId()).size());
        assertEquals(1, registry.listVehiclesOwnedBy(buyer.getId()).size());
    }

    @Test
    @DisplayName("A license is issued to an adult and is valid today")
    void issuesLicenseToAdult() {
        Person adult = registry.registerPerson("Adult", "Driver", LocalDate.of(1990, 1, 1));

        DriverLicense license = registry.issueLicense(adult.getId());

        assertEquals(adult.getId(), license.getHolder().getId());
        assertTrue(license.isValidOn(LocalDate.now()));
    }

    @Test
    @DisplayName("A license is refused to someone under the driving age")
    void refusesLicenseToMinor() {
        Person child = registry.registerPerson("Too", "Young", LocalDate.now().minusYears(1));

        assertThrows(RegistryException.class, () -> registry.issueLicense(child.getId()));
    }
}
