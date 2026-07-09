package gov.dmv.registry.model;

import java.util.Objects;

/**
 * A Vehicle registered with the DMV. Each vehicle is OWNED by exactly one
 * Person at a time.
 *
 * HOW WE LINK OBJECTS TOGETHER: notice we store {@code ownerId} (a String) and
 * NOT a whole Person object. In systems backed by a database this is how rows
 * reference each other - by id, like a foreign key. It keeps our objects small
 * and avoids tangled webs of objects pointing at each other. To get the actual
 * owner, code asks the repository: "give me the Person with this id".
 */
public class Vehicle {

    /** System-assigned unique id, e.g. "V-1". */
    private final String id;

    /**
     * Vehicle Identification Number - the real-world unique code stamped on
     * every vehicle. We treat it as unique across the whole registry.
     */
    private final String vin;

    private final String make;   // manufacturer, e.g. "Toyota"
    private final String model;  // model name, e.g. "Corolla"
    private final int year;      // model year, e.g. 2020
    private final VehicleType type;

    // This one is NOT final: ownership can CHANGE when a vehicle is sold/
    // transferred. So we allow it to be updated (through a controlled method).
    private String ownerId;

    public Vehicle(String id, String vin, String make, String model, int year,
                   VehicleType type, String ownerId) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.vin = requireText(vin, "vin");
        this.make = requireText(make, "make");
        this.model = requireText(model, "model");
        this.type = Objects.requireNonNull(type, "type is required");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId is required");
        this.year = year;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    // ---- Getters ---------------------------------------------------------

    public String getId() {
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

    public String getOwnerId() {
        return ownerId;
    }

    // ---- Controlled change: transfer ownership ---------------------------
    // We expose ONE narrow method to change the owner rather than a generic
    // "setOwnerId". This makes the intent obvious in the code that calls it and
    // gives us a single place to guard the value.
    public void transferTo(String newOwnerId) {
        this.ownerId = Objects.requireNonNull(newOwnerId, "newOwnerId is required");
    }

    @Override
    public String toString() {
        return "Vehicle{" + id + ", " + year + " " + make + " " + model
                + " (" + type + "), VIN " + vin + ", owner " + ownerId + "}";
    }
}
