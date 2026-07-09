package gov.dmv.registry.repository;

import gov.dmv.registry.model.DriverLicense;

import java.util.List;
import java.util.Optional;

/**
 * The filing cabinet for driver licenses.
 *
 * Design note: in this first version we assume a person holds at most ONE
 * license, so {@code findByHolderId} returns a single Optional. If that rule
 * ever changes, this is the one place we would revisit.
 */
public interface DriverLicenseRepository {

    DriverLicense save(DriverLicense license);

    Optional<DriverLicense> findByNumber(String number);

    Optional<DriverLicense> findByHolderId(String holderId);

    List<DriverLicense> findAll();

    long count();
}
