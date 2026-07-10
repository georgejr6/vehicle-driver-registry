package gov.dmv.registry.repository;

import gov.dmv.registry.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Storage for Vehicles (Spring Data JPA).
 *
 * DERIVED QUERIES - the magic trick: Spring reads the METHOD NAME and writes
 * the database query for you. You never write SQL for these.
 *
 *   findByVin(vin)        -> "SELECT * FROM vehicle WHERE vin = ?"
 *   findByOwnerId(ownerId)-> "...WHERE owner_id = ?"  (owner.id, via the
 *                            @ManyToOne relationship on Vehicle)
 *
 * The return types matter too:
 *   Optional<Vehicle> = "zero or one match" (VIN is unique)
 *   List<Vehicle>     = "any number of matches" (an owner may have many cars)
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /** Find a vehicle by its unique VIN (used to enforce VIN uniqueness). */
    Optional<Vehicle> findByVin(String vin);

    /** All vehicles owned by the person with this id. */
    List<Vehicle> findByOwnerId(Long ownerId);
}
