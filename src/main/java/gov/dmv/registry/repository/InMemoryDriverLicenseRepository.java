package gov.dmv.registry.repository;

import gov.dmv.registry.model.DriverLicense;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of {@link DriverLicenseRepository}. Keyed by the
 * license number.
 */
public class InMemoryDriverLicenseRepository implements DriverLicenseRepository {

    private final Map<String, DriverLicense> storage = new LinkedHashMap<>();

    @Override
    public DriverLicense save(DriverLicense license) {
        storage.put(license.getNumber(), license);
        return license;
    }

    @Override
    public Optional<DriverLicense> findByNumber(String number) {
        return Optional.ofNullable(storage.get(number));
    }

    @Override
    public Optional<DriverLicense> findByHolderId(String holderId) {
        return storage.values().stream()
                .filter(l -> l.getHolderId().equals(holderId))
                .findFirst();
    }

    @Override
    public List<DriverLicense> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public long count() {
        return storage.size();
    }
}
