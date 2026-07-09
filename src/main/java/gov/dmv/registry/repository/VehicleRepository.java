package gov.dmv.registry.repository;

import gov.dmv.registry.model.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * The filing cabinet for Vehicles. Same idea as {@link PersonRepository}, but
 * with a couple of extra look-ups that only make sense for vehicles: finding by
 * VIN, and listing everything a particular owner has.
 */
public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(String id);

    /** Find a vehicle by its real-world VIN (used to enforce VIN uniqueness). */
    Optional<Vehicle> findByVin(String vin);

    List<Vehicle> findAll();

    /** Every vehicle currently owned by the person with this id. */
    List<Vehicle> findByOwnerId(String ownerId);

    boolean deleteById(String id);

    long count();
}
