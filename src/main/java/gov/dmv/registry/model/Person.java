package gov.dmv.registry.model;

// "import" pulls in classes from Java's standard library so we can use them by
// their short name. LocalDate = a calendar date (year-month-day) with no time.
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * A Person is a human known to the DMV: a vehicle owner, a license holder, or
 * both. In enterprise systems we call classes like this "domain models" or
 * "entities" - they represent the real-world THINGS the system is about.
 *
 * DESIGN CHOICE: most fields here are "final", meaning they are set once (in the
 * constructor) and never change. A person's date of birth, for example, does
 * not change. Making data unchangeable ("immutable") wherever possible removes
 * a whole category of bugs and is a hallmark of clean, reliable code.
 */
public class Person {

    // ---- FIELDS: the data each Person object carries. --------------------
    // "private" = only code INSIDE this class may touch these directly. The
    // outside world must go through the methods below. This is "encapsulation":
    // the object protects its own data.

    /** A unique id assigned by the system, e.g. "P-1". Acts like a case number. */
    private final String id;

    private final String firstName;
    private final String lastName;

    /** Date of birth - lets us calculate age for rules like the driving age. */
    private final LocalDate dateOfBirth;

    // ---- CONSTRUCTOR: the "factory" that builds a Person object. ---------
    // You call it with:  new Person("P-1", "Ada", "Lovelace", someDate);
    // It also VALIDATES the inputs so we can never create a broken Person.
    public Person(String id, String firstName, String lastName, LocalDate dateOfBirth) {
        // Objects.requireNonNull throws an error immediately if someone passes
        // null (nothing) where we require a real value. Failing fast here makes
        // bugs obvious right away instead of hours later somewhere else.
        this.id = Objects.requireNonNull(id, "id is required");
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "dateOfBirth is required");
    }

    // A small private helper so we don't repeat the same "must not be blank"
    // check for every text field. Private = used only inside this class.
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            // IllegalArgumentException is Java's standard way of saying
            // "you called me with a bad argument".
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim(); // trim() removes accidental leading/trailing spaces.
    }

    // ---- GETTERS: the ONLY way outside code reads our private fields. -----
    // This "getX()" naming is a universal Java convention (a "JavaBean").
    // Tools, frameworks, and IntelliJ all recognise it.

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    // ---- DERIVED VALUES: information we CALCULATE rather than store. ------

    /** Convenience: full name as one string, e.g. "Ada Lovelace". */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Age in whole years, measured against the date you pass in. We take "today"
     * as a parameter instead of calling the clock ourselves - this makes the
     * method easy to test (a test can pretend it is any date) and is a good
     * habit for time-based logic.
     */
    public int getAgeOn(LocalDate today) {
        // Period.between figures out the gap between two dates; getYears() gives
        // the whole-years part of that gap.
        return Period.between(dateOfBirth, today).getYears();
    }

    // ---- toString: a readable text version of this object. ---------------
    // Java calls this automatically when you print an object. Overriding it
    // gives friendly output like "Person{P-1, Ada Lovelace}" instead of the
    // cryptic default "gov.dmv.registry.model.Person@1b6d3586".
    @Override
    public String toString() {
        return "Person{" + id + ", " + getFullName() + ", born " + dateOfBirth + "}";
    }
}
