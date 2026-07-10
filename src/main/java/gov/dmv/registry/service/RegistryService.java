package gov.dmv.registry.service;

import gov.dmv.registry.model.DriverLicense;
import gov.dmv.registry.model.LicenseStatus;
import gov.dmv.registry.model.Person;
import gov.dmv.registry.model.Vehicle;
import gov.dmv.registry.model.VehicleType;
import gov.dmv.registry.repository.DriverLicenseRepository;
import gov.dmv.registry.repository.PersonRepository;
import gov.dmv.registry.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * The SERVICE layer - the "brain" holding the DMV's business RULES. This is the
 * layer that barely changed when we modernized: the rules about age limits,
 * unique VINs and ownership transfer are the same as in the plain-Java version.
 * Only the storage underneath swapped from in-memory maps to a real database.
 * That is the entire payoff of building in layers.
 *
 * Two Spring annotations do a lot of work here:
 *
 *   @Service     marks this as a business component. At startup Spring creates
 *                ONE shared instance and hands it to whoever needs it.
 *
 *   Constructor injection: Spring sees the constructor needs three repositories
 *                and passes them in automatically ("dependency injection"). We
 *                never call 'new RegistryService(...)' ourselves.
 *
 *   @Transactional  wraps a method in a database transaction: everything inside
 *                succeeds together or is rolled back together, so the database
 *                never ends up half-updated.
 */
@Service
public class RegistryService {

    // ---- Business-rule constants (one clear place to change each policy). --
    private static final int MINIMUM_DRIVING_AGE = 16;
    private static final int EARLIEST_VEHICLE_YEAR = 1886; // the first automobile
    private static final int LICENSE_VALID_YEARS = 5;

    private final PersonRepository people;
    private final VehicleRepository vehicles;
    private final DriverLicenseRepository licenses;

    public RegistryService(PersonRepository people,
                           VehicleRepository vehicles,
                           DriverLicenseRepository licenses) {
        this.people = people;
        this.vehicles = vehicles;
        this.licenses = licenses;
    }

    // =====================================================================
    //  PEOPLE
    // =====================================================================

    @Transactional
    public Person registerPerson(String firstName, String lastName, LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new RegistryException("Date of birth cannot be in the future.");
        }
        // The Person constructor validates the names; the repository saves it
        // and the database fills in the generated id.
        Person person = new Person(firstName, lastName, dateOfBirth);
        return people.save(person);
    }

    // @Transactional(readOnly=true) is a small optimisation/hint for look-ups
    // that only read and never change data.
    @Transactional(readOnly = true)
    public List<Person> listPeople() {
        return people.findAll();
    }

    /** Fetch a person or throw a clear error. Reused by many rules below. */
    @Transactional(readOnly = true)
    public Person requirePerson(Long personId) {
        return people.findById(personId)
                .orElseThrow(() -> new RegistryException("No person found with id " + personId));
    }

    // =====================================================================
    //  VEHICLES
    // =====================================================================

    @Transactional
    public Vehicle registerVehicle(Long ownerId, String vin, String make,
                                   String model, int year, VehicleType type) {
        Person owner = requirePerson(ownerId);

        int nextYear = LocalDate.now().getYear() + 1;
        if (year < EARLIEST_VEHICLE_YEAR || year > nextYear) {
            throw new RegistryException(
                    "Vehicle year " + year + " is out of range ("
                            + EARLIEST_VEHICLE_YEAR + "-" + nextYear + ").");
        }

        // Rule: no two vehicles may share a VIN. We check first to give a clear
        // message (the database's unique constraint is a second safety net).
        vehicles.findByVin(vin).ifPresent(existing -> {
            throw new RegistryException("A vehicle with VIN " + vin + " already exists.");
        });

        Vehicle vehicle = new Vehicle(vin, make, model, year, type, owner);
        return vehicles.save(vehicle);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listVehicles() {
        return vehicles.findAll();
    }

    @Transactional(readOnly = true)
    public List<Vehicle> listVehiclesOwnedBy(Long ownerId) {
        requirePerson(ownerId);
        return vehicles.findByOwnerId(ownerId);
    }

    @Transactional
    public Vehicle transferVehicle(Long vehicleId, Long newOwnerId) {
        Vehicle vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> new RegistryException("No vehicle found with id " + vehicleId));
        Person newOwner = requirePerson(newOwnerId);

        vehicle.transferTo(newOwner);
        return vehicles.save(vehicle);
    }

    // =====================================================================
    //  DRIVER LICENSES
    // =====================================================================

    @Transactional
    public DriverLicense issueLicense(Long holderId) {
        Person holder = requirePerson(holderId);
        LocalDate today = LocalDate.now();

        int age = Period.between(holder.getDateOfBirth(), today).getYears();
        if (age < MINIMUM_DRIVING_AGE) {
            throw new RegistryException(
                    holder.getFullName() + " is " + age + "; must be at least "
                            + MINIMUM_DRIVING_AGE + " to be licensed.");
        }

        licenses.findByHolderId(holderId).ifPresent(existing -> {
            throw new RegistryException(
                    holder.getFullName() + " already holds license " + existing.getLicenseNumber() + ".");
        });

        LocalDate expiry = today.plusYears(LICENSE_VALID_YEARS);
        DriverLicense license = new DriverLicense(holder, today, expiry, LicenseStatus.ACTIVE);
        return licenses.save(license);
    }

    @Transactional
    public DriverLicense suspendLicense(Long licenseId) {
        DriverLicense license = licenses.findById(licenseId)
                .orElseThrow(() -> new RegistryException("No license found with id " + licenseId));
        license.setStatus(LicenseStatus.SUSPENDED);
        return licenses.save(license);
    }

    @Transactional(readOnly = true)
    public List<DriverLicense> listLicenses() {
        return licenses.findAll();
    }
}
