package gov.dmv.registry.service;

import gov.dmv.registry.model.DriverLicense;
import gov.dmv.registry.model.LicenseStatus;
import gov.dmv.registry.model.Person;
import gov.dmv.registry.model.Vehicle;
import gov.dmv.registry.model.VehicleType;
import gov.dmv.registry.repository.DriverLicenseRepository;
import gov.dmv.registry.repository.PersonRepository;
import gov.dmv.registry.repository.VehicleRepository;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * The SERVICE layer: the "brain" of the application. This is where the DMV's
 * business RULES live - the policies a government office would actually enforce.
 *
 * WHERE EACH LAYER FITS:
 *   - model      : the data (Person, Vehicle, DriverLicense)
 *   - repository : storing/fetching that data
 *   - service    : the rules and workflows that tie it together  <-- YOU ARE HERE
 *   - App        : the user-facing menu that calls the service
 *
 * KEY IDEA - "dependency injection": this class does not CREATE its own
 * repositories. Instead they are handed to it in the constructor. That means we
 * can give it the in-memory repositories for real use, OR give a test its own
 * fresh repositories, OR (later) give it database-backed ones - without changing
 * a single line in here. The service only cares about the interface contract,
 * never the concrete storage.
 */
public class RegistryService {

    // ---- Business-rule constants: named numbers, so the rules read clearly
    //      and there is exactly one place to change each policy. --------------

    /** Youngest age (in years) at which the DMV will issue a license. */
    private static final int MINIMUM_DRIVING_AGE = 16;

    /** Earliest plausible model year (1886 = the first automobile). Guards typos. */
    private static final int EARLIEST_VEHICLE_YEAR = 1886;

    /** How many years a newly issued license stays valid before renewal. */
    private static final int LICENSE_VALID_YEARS = 5;

    // ---- The collaborators this service depends on (the three filing cabinets).
    private final PersonRepository people;
    private final VehicleRepository vehicles;
    private final DriverLicenseRepository licenses;

    /**
     * Constructor - the repositories are "injected" (passed in) here.
     */
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

    /**
     * Register a new person in the DMV. Validates the input, generates a fresh
     * id, stores the person, and hands the created object back.
     */
    public Person registerPerson(String firstName, String lastName, LocalDate dateOfBirth) {
        // Rule: a person cannot be born in the future.
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new RegistryException("Date of birth cannot be in the future.");
        }
        // Build a friendly, sequential id like "P-1", "P-2", ...
        String id = "P-" + (people.count() + 1);

        // The Person constructor does the "not blank" checks for names, so we
        // let it throw if the names are bad, then store the result.
        Person person = new Person(id, firstName, lastName, dateOfBirth);
        return people.save(person);
    }

    public List<Person> listPeople() {
        return people.findAll();
    }

    /**
     * Fetch a person or throw a clear error if they do not exist. Many rules
     * below reuse this, so "the owner must exist" is written exactly once.
     */
    public Person requirePerson(String personId) {
        return people.findById(personId)
                .orElseThrow(() -> new RegistryException("No person found with id " + personId));
    }

    // =====================================================================
    //  VEHICLES
    // =====================================================================

    /**
     * Register a vehicle to an existing owner. Enforces: owner must exist, model
     * year must be sensible, and the VIN must be unique across the registry.
     */
    public Vehicle registerVehicle(String ownerId, String vin, String make,
                                   String model, int year, VehicleType type) {
        // Rule: the owner has to be a real, known person.
        Person owner = requirePerson(ownerId);

        // Rule: the model year must be within a believable range. We allow
        // "next year" because dealers sell next-year models early.
        int nextYear = LocalDate.now().getYear() + 1;
        if (year < EARLIEST_VEHICLE_YEAR || year > nextYear) {
            throw new RegistryException(
                    "Vehicle year " + year + " is out of range ("
                            + EARLIEST_VEHICLE_YEAR + "-" + nextYear + ").");
        }

        // Rule: no two vehicles may share a VIN.
        vehicles.findByVin(vin).ifPresent(existing -> {
            throw new RegistryException("A vehicle with VIN " + vin + " already exists.");
        });

        String id = "V-" + (vehicles.count() + 1);
        Vehicle vehicle = new Vehicle(id, vin, make, model, year, type, owner.getId());
        return vehicles.save(vehicle);
    }

    public List<Vehicle> listVehicles() {
        return vehicles.findAll();
    }

    public List<Vehicle> listVehiclesOwnedBy(String ownerId) {
        requirePerson(ownerId); // fail clearly if the owner id is unknown
        return vehicles.findByOwnerId(ownerId);
    }

    /**
     * Transfer a vehicle to a new owner (e.g. when it is sold). Both the vehicle
     * and the new owner must exist.
     */
    public Vehicle transferVehicle(String vehicleId, String newOwnerId) {
        Vehicle vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> new RegistryException("No vehicle found with id " + vehicleId));
        Person newOwner = requirePerson(newOwnerId);

        vehicle.transferTo(newOwner.getId()); // change ownership on the object
        return vehicles.save(vehicle);        // persist the change
    }

    // =====================================================================
    //  DRIVER LICENSES
    // =====================================================================

    /**
     * Issue a driver's license to a person. Enforces: the person exists, is old
     * enough, and does not already hold a license.
     */
    public DriverLicense issueLicense(String holderId) {
        Person holder = requirePerson(holderId);

        LocalDate today = LocalDate.now();

        // Rule: applicant must meet the minimum driving age.
        int age = Period.between(holder.getDateOfBirth(), today).getYears();
        if (age < MINIMUM_DRIVING_AGE) {
            throw new RegistryException(
                    holder.getFullName() + " is " + age + "; must be at least "
                            + MINIMUM_DRIVING_AGE + " to be licensed.");
        }

        // Rule: one license per person (in this version of the system).
        licenses.findByHolderId(holderId).ifPresent(existing -> {
            throw new RegistryException(
                    holder.getFullName() + " already holds license " + existing.getNumber() + ".");
        });

        String number = "DL-" + (licenses.count() + 1);
        LocalDate expiry = today.plusYears(LICENSE_VALID_YEARS);
        DriverLicense license =
                new DriverLicense(number, holder.getId(), today, expiry, LicenseStatus.ACTIVE);
        return licenses.save(license);
    }

    /** Suspend a license (e.g. after violations). Must currently exist. */
    public DriverLicense suspendLicense(String licenseNumber) {
        DriverLicense license = licenses.findByNumber(licenseNumber)
                .orElseThrow(() -> new RegistryException("No license found: " + licenseNumber));
        license.setStatus(LicenseStatus.SUSPENDED);
        return licenses.save(license);
    }

    public List<DriverLicense> listLicenses() {
        return licenses.findAll();
    }
}
