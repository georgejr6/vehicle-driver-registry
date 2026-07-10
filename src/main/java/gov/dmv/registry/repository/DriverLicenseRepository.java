package gov.dmv.registry.repository;

import gov.dmv.registry.model.DriverLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Storage for driver licenses (Spring Data JPA).
 *
 * Because a person holds at most one license, the look-up by holder returns an
 * {@link Optional} (zero or one), not a list.
 */
public interface DriverLicenseRepository extends JpaRepository<DriverLicense, Long> {

    /** The license held by the person with this id, if any. */
    Optional<DriverLicense> findByHolderId(Long holderId);
}
