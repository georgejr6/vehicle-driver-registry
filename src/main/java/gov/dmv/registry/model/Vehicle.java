package gov.dmv.registry.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * A Vehicle registered with the DMV.
 *
 * THE INTERESTING PART - A REAL RELATIONSHIP:
 * A vehicle is owned by a Person. In the database this becomes a FOREIGN KEY:
 * the vehicle row stores the id of its owner's row. JPA models that with
 * {@code @ManyToOne} ("MANY vehicles can point to ONE person"). Instead of
 * juggling a raw id ourselves, we can now just call {@code vehicle.getOwner()}
 * and JPA fetches the whole Person for us. This is how relational databases
 * link tables together.
 */
@Entity
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The real-world Vehicle Identification Number. @Column(unique = true) asks
     * the database to REFUSE two vehicles with the same VIN - a safety net
     * enforced by the database itself, on top of our own check in the service.
     */
    @Column(unique = true, nullable = false)
    private String vin;

    private String make;
    private String model;

    /**
     * The model year. NOTE: we must map this to a column NOT called "year",
     * because YEAR is a reserved keyword in SQL/H2 - letting the database name
     * the column "year" makes the generated CREATE TABLE statement a syntax
     * error. @Column(name = ...) renames just the database column; our Java
     * field stays the natural "year". (A classic gotcha worth remembering.)
     */
    @Column(name = "model_year", nullable = false)
    private int year;

    /**
     * Store the enum as readable TEXT ("CAR") in the database instead of a
     * number. @Enumerated(STRING) makes the data human-readable and safe to
     * reorder later. Without it, JPA would store 0/1/2 and reordering the enum
     * would silently corrupt meaning.
     */
    @Enumerated(EnumType.STRING)
    private VehicleType type;

    /**
     * The owner relationship. @JoinColumn names the foreign-key column that
     * holds the owner's id in the vehicle table.
     */
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Person owner;

    protected Vehicle() {
    }

    public Vehicle(String vin, String make, String model, int year,
                   VehicleType type, Person owner) {
        this.vin = requireText(vin, "vin");
        this.make = requireText(make, "make");
        this.model = requireText(model, "model");
        this.year = year;
        this.type = Objects.requireNonNull(type, "type is required");
        this.owner = Objects.requireNonNull(owner, "owner is required");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    // ---- Getters ---------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getVin() {
        return vin;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public VehicleType getType() {
        return type;
    }

    public Person getOwner() {
        return owner;
    }

    // ---- Controlled change: transfer ownership ---------------------------
    public void transferTo(Person newOwner) {
        this.owner = Objects.requireNonNull(newOwner, "newOwner is required");
    }

    @Override
    public String toString() {
        return "Vehicle{" + id + ", " + year + " " + make + " " + model
                + " (" + type + "), VIN " + vin + "}";
    }
}
