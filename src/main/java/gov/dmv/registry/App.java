package gov.dmv.registry;

import gov.dmv.registry.model.Person;
import gov.dmv.registry.model.Vehicle;
import gov.dmv.registry.model.VehicleType;
import gov.dmv.registry.repository.DriverLicenseRepository;
import gov.dmv.registry.repository.InMemoryDriverLicenseRepository;
import gov.dmv.registry.repository.InMemoryPersonRepository;
import gov.dmv.registry.repository.InMemoryVehicleRepository;
import gov.dmv.registry.repository.PersonRepository;
import gov.dmv.registry.repository.VehicleRepository;
import gov.dmv.registry.service.RegistryException;
import gov.dmv.registry.service.RegistryService;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * App = the program's STARTING POINT and, for now, its user interface.
 *
 * Every Java program begins in a method called {@code main}. When you press the
 * green "Run" arrow next to this class in IntelliJ, Java calls main() and the
 * app comes to life.
 *
 * This class deliberately contains NO business rules. Its only jobs are:
 *   1. Wire the pieces together (create the repositories + service).
 *   2. Read the user's menu choices from the keyboard.
 *   3. Call the service and print the results.
 *
 * That separation is intentional: when we later replace this text menu with a
 * real web page, we will throw THIS file away and keep everything else. The
 * brain (service) and data (model/repository) do not depend on the interface.
 */
public class App {

    // A Scanner reads what the user types in the terminal. We keep one shared
    // instance for the whole program.
    private static final Scanner INPUT = new Scanner(System.in);

    // A single reference to our service, set up in main().
    private static RegistryService registry;

    public static void main(String[] args) {
        // --- 1. WIRING: build the storage layer, then the service on top. ---
        // Notice we declare the variables using the INTERFACE type on the left
        // and pick the concrete in-memory class on the right. Code that follows
        // only sees the interface - so swapping storage later touches just here.
        PersonRepository people = new InMemoryPersonRepository();
        VehicleRepository vehicles = new InMemoryVehicleRepository();
        DriverLicenseRepository licenses = new InMemoryDriverLicenseRepository();
        registry = new RegistryService(people, vehicles, licenses);

        // --- 2. Put some demo data in so the menus aren't empty on first run.
        seedDemoData();

        System.out.println("=====================================================");
        System.out.println("  DMV Vehicle & Driver Registry  (console edition)   ");
        System.out.println("=====================================================");

        // --- 3. The main loop: show the menu, act on the choice, repeat. ----
        boolean running = true;
        while (running) {
            printMenu();
            String choice = INPUT.nextLine().trim();

            // We wrap each action in try/catch so a broken business rule shows a
            // friendly message instead of crashing the whole program.
            try {
                switch (choice) {
                    case "1" -> doRegisterPerson();
                    case "2" -> doListPeople();
                    case "3" -> doRegisterVehicle();
                    case "4" -> doListVehicles();
                    case "5" -> doTransferVehicle();
                    case "6" -> doIssueLicense();
                    case "7" -> doListLicenses();
                    case "0" -> running = false;
                    default  -> System.out.println("Unknown option. Please pick from the menu.");
                }
            } catch (RegistryException e) {
                // A rule was violated - show WHY, then loop back to the menu.
                System.out.println("  ! Cannot do that: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                // Bad input (e.g. blank name) - same friendly treatment.
                System.out.println("  ! Invalid input: " + e.getMessage());
            }
        }

        System.out.println("Goodbye. (Remember: data was in memory only, so it is now gone.)");
    }

    // ---------------------------------------------------------------------
    //  MENU + individual actions
    // ---------------------------------------------------------------------

    private static void printMenu() {
        System.out.println();
        System.out.println("-------------------- MENU ---------------------------");
        System.out.println(" 1) Register a person");
        System.out.println(" 2) List people");
        System.out.println(" 3) Register a vehicle");
        System.out.println(" 4) List vehicles");
        System.out.println(" 5) Transfer a vehicle to a new owner");
        System.out.println(" 6) Issue a driver license");
        System.out.println(" 7) List driver licenses");
        System.out.println(" 0) Exit");
        System.out.print("Choose an option: ");
    }

    private static void doRegisterPerson() {
        String first = ask("First name");
        String last = ask("Last name");
        LocalDate dob = askDate("Date of birth (YYYY-MM-DD)");
        Person p = registry.registerPerson(first, last, dob);
        System.out.println("  + Registered " + p);
    }

    private static void doListPeople() {
        System.out.println("People on file:");
        for (Person p : registry.listPeople()) {
            System.out.println("   - " + p);
        }
    }

    private static void doRegisterVehicle() {
        String ownerId = ask("Owner id (e.g. P-1)");
        String vin = ask("VIN");
        String make = ask("Make");
        String model = ask("Model");
        int year = askInt("Year");
        VehicleType type = askVehicleType();
        Vehicle v = registry.registerVehicle(ownerId, vin, make, model, year, type);
        System.out.println("  + Registered " + v);
    }

    private static void doListVehicles() {
        System.out.println("Vehicles on file:");
        for (Vehicle v : registry.listVehicles()) {
            System.out.println("   - " + v);
        }
    }

    private static void doTransferVehicle() {
        String vehicleId = ask("Vehicle id (e.g. V-1)");
        String newOwnerId = ask("New owner id (e.g. P-2)");
        Vehicle v = registry.transferVehicle(vehicleId, newOwnerId);
        System.out.println("  + Transfer complete: " + v);
    }

    private static void doIssueLicense() {
        String holderId = ask("Person id to license (e.g. P-1)");
        System.out.println("  + Issued " + registry.issueLicense(holderId));
    }

    private static void doListLicenses() {
        System.out.println("Driver licenses on file:");
        registry.listLicenses().forEach(l -> System.out.println("   - " + l));
    }

    // ---------------------------------------------------------------------
    //  Small input helpers - keep the actions above short and readable.
    // ---------------------------------------------------------------------

    /** Print a prompt and return the trimmed line the user types. */
    private static String ask(String prompt) {
        System.out.print("  " + prompt + ": ");
        return INPUT.nextLine().trim();
    }

    /** Keep asking until the user types a valid whole number. */
    private static int askInt(String prompt) {
        while (true) {
            String raw = ask(prompt);
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("    (Please enter a whole number.)");
            }
        }
    }

    /** Keep asking until the user types a valid date like 1990-05-21. */
    private static LocalDate askDate(String prompt) {
        while (true) {
            String raw = ask(prompt);
            try {
                return LocalDate.parse(raw); // expects the ISO format YYYY-MM-DD
            } catch (Exception e) {
                System.out.println("    (Please use the format YYYY-MM-DD, e.g. 1990-05-21.)");
            }
        }
    }

    /** Let the user pick a VehicleType by name, retrying on typos. */
    private static VehicleType askVehicleType() {
        while (true) {
            String raw = ask("Type " + java.util.Arrays.toString(VehicleType.values())).toUpperCase();
            try {
                return VehicleType.valueOf(raw); // matches one of the enum names
            } catch (IllegalArgumentException e) {
                System.out.println("    (Please type one of the listed values.)");
            }
        }
    }

    // ---------------------------------------------------------------------
    //  Demo data so the app has something to show immediately.
    // ---------------------------------------------------------------------
    private static void seedDemoData() {
        Person ada = registry.registerPerson("Ada", "Lovelace", LocalDate.of(1990, 12, 10));
        Person alan = registry.registerPerson("Alan", "Turing", LocalDate.of(1985, 6, 23));

        registry.registerVehicle(ada.getId(), "1HGBH41JXMN109186",
                "Toyota", "Corolla", 2020, VehicleType.CAR);
        registry.registerVehicle(alan.getId(), "JH4KA8260MC000000",
                "Honda", "CB500", 2019, VehicleType.MOTORCYCLE);

        registry.issueLicense(ada.getId());
    }
}
