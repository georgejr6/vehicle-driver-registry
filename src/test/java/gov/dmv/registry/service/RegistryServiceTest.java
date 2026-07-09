package gov.dmv.registry.service;

import gov.dmv.registry.model.DriverLicense;
import gov.dmv.registry.model.Person;
import gov.dmv.registry.model.Vehicle;
import gov.dmv.registry.model.VehicleType;
import gov.dmv.registry.repository.InMemoryDriverLicenseRepository;
import gov.dmv.registry.repository.InMemoryPersonRepository;
import gov.dmv.registry.repository.InMemoryVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

// "static import" lets us call assertEquals(...) instead of
// Assertions.assertEquals(...) - just less typing in tests.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AUTOMATED TESTS for RegistryService.
 *
 * A test is code that runs OUR code and checks it behaves as expected. When your
 * teammate opens this in IntelliJ, she can right-click the class and choose
 * "Run tests" - green means every rule still works, red means something broke.
 * Tests are how professionals change code confidently without fear of silently
 * breaking things.
 *
 * Each test builds a BRAND-NEW service with fresh, empty in-memory repositories
 * (see setUp below), so tests never interfere with one another.
 */
class RegistryServiceTest {

    private RegistryService registry;

    // @BeforeEach = JUnit runs this method before EVERY single test, giving each
    // one a clean slate.
    @BeforeEach
    void setUp() {
        registry = new RegistryService(
                new InMemoryPersonRepository(),
                new InMemoryVehicleRepository(),
                new InMemoryDriverLicenseRepository());
    }

    @Test
    @DisplayName("Registering a person stores them and assigns an id")
    void registersPerson() {
        Person p = registry.registerPerson("Grace", "Hopper", LocalDate.of(1980, 1, 1));

        // assertEquals(expected, actual) - the test PASSES when they match.
        assertEquals("Grace Hopper", p.getFullName());
        assertEquals(1, registry.listPeople().size());
    }

    @Test
    @DisplayName("Registering a vehicle to an unknown owner is rejected")
    void rejectsVehicleForUnknownOwner() {
        // assertThrows checks that the code inside DOES throw the given error.
        // Here: registering to owner "P-999" (who doesn't exist) must fail.
        assertThrows(RegistryException.class, () ->
                registry.registerVehicle("P-999", "VIN123", "Ford", "F-150", 2021, VehicleType.TRUCK));
    }

    @Test
    @DisplayName("Two vehicles cannot share the same VIN")
    void rejectsDuplicateVin() {
        Person owner = registry.registerPerson("Katherine", "Johnson", LocalDate.of(1975, 8, 26));
        registry.registerVehicle(owner.getId(), "SAMEVIN", "Tesla", "Model 3", 2022, VehicleType.CAR);

        // The second registration reuses the VIN and must be blocked.
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

        assertEquals(buyer.getId(), moved.getOwnerId());
        // The seller should now own nothing; the buyer should own one vehicle.
        assertEquals(0, registry.listVehiclesOwnedBy(seller.getId()).size());
        assertEquals(1, registry.listVehiclesOwnedBy(buyer.getId()).size());
    }

    @Test
    @DisplayName("A license is issued to an adult and is valid today")
    void issuesLicenseToAdult() {
        Person adult = registry.registerPerson("Adult", "Driver", LocalDate.of(1990, 1, 1));

        DriverLicense license = registry.issueLicense(adult.getId());

        assertEquals(adult.getId(), license.getHolderId());
        assertTrue(license.isValidOn(LocalDate.now()));
    }

    @Test
    @DisplayName("A license is refused to someone under the driving age")
    void refusesLicenseToMinor() {
        // Born last year -> age 1 -> well under the minimum driving age.
        Person child = registry.registerPerson("Too", "Young", LocalDate.now().minusYears(1));

        assertThrows(RegistryException.class, () -> registry.issueLicense(child.getId()));
    }
}
