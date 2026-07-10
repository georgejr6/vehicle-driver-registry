package gov.dmv.registry.model;

// jakarta.persistence.* holds the JPA annotations that map this class to a
// database TABLE. "JPA" = Java Persistence API, the standard way Java objects
// are saved into relational databases.
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.Period;

/**
 * A Person known to the DMV (an owner, a license holder, or both).
 *
 * WHAT CHANGED WHEN WE "MODERNIZED":
 * This used to be a plain object we stored in a HashMap. Now it is a JPA
 * ENTITY: the annotations below tell Spring/Hibernate to create a matching
 * database table and to load/save rows as Person objects automatically. The
 * business meaning of the class did not change - only how it is stored.
 *
 * Two JPA rules to know:
 *   1. An entity needs a no-argument constructor (JPA uses it to rebuild
 *      objects when reading rows). We make it 'protected' so our own code is
 *      still nudged toward the proper, validating constructor.
 *   2. Fields can no longer be 'final', because JPA sets them when loading.
 */
@Entity                       // "this class is a database table"
@Table(name = "person")       // name the table explicitly (optional but tidy)
public class Person {

    /**
     * Primary key - the unique row number the DATABASE assigns.
     * @GeneratedValue(IDENTITY) = "let the database auto-increment this for us"
     * (1, 2, 3, ...). We use a Long that starts out null and is filled in on save.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    /** No-arg constructor required by JPA. Not meant for our own code. */
    protected Person() {
    }

    /** The constructor OUR code uses. It validates before building. */
    public Person(String firstName, String lastName, LocalDate dateOfBirth) {
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("dateOfBirth is required");
        }
        this.dateOfBirth = dateOfBirth;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    // ---- Getters (how other code reads the data) -------------------------

    public Long getId() {
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

    // ---- Derived values (calculated, not stored) -------------------------

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Age in whole years measured against the date you pass in (testable). */
    public int getAgeOn(LocalDate today) {
        return Period.between(dateOfBirth, today).getYears();
    }

    @Override
    public String toString() {
        return "Person{" + id + ", " + getFullName() + "}";
    }
}
