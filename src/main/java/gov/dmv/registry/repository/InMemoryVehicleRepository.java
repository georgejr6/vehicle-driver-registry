package gov.dmv.registry.repository;

import gov.dmv.registry.model.Vehicle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of {@link VehicleRepository}.
 *
 * This class shows a useful technique: some look-ups (findByVin, findByOwnerId)
 * are not a simple key fetch, so we SCAN the stored vehicles and keep the ones
 * that match, using Java's "Stream" API. A stream is a pipeline that lets you
 * filter/transform a collection in a readable way. In a real database these
 * same look-ups would become indexed queries - but the behaviour is identical.
 */
public class InMemoryVehicleRepository implements VehicleRepository {

    private final Map<String, Vehicle> storage = new LinkedHashMap<>();

    @Override
    public Vehicle save(Vehicle vehicle) {
        storage.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Vehicle> findByVin(String vin) {
        return storage.values().stream()          // look at every stored vehicle
                .filter(v -> v.getVin().equalsIgnoreCase(vin)) // keep VIN matches
                .findFirst();                      // grab the first one (if any)
    }

    @Override
    public List<Vehicle> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Vehicle> findByOwnerId(String ownerId) {
        return storage.values().stream()
                .filter(v -> v.getOwnerId().equals(ownerId)) // keep this owner's
                .toList();                                   // collect into a list
    }

    @Override
    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }

    @Override
    public long count() {
        return storage.size();
    }
}
