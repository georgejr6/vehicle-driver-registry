package gov.dmv.registry.repository;

import gov.dmv.registry.model.Person;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A concrete PersonRepository that keeps everything in the computer's memory,
 * using a Map (a lookup table of key -> value). The key is the person's id and
 * the value is the Person object.
 *
 * "implements PersonRepository" is us PROMISING to provide every method the
 * interface listed. If we forget one, the code will not compile - the compiler
 * enforces the contract for us.
 *
 * This version is intentionally simple: great for learning and for tests,
 * but the data vanishes when the program stops. A future increment will add a
 * database-backed version implementing the very same PersonRepository interface.
 */
public class InMemoryPersonRepository implements PersonRepository {

    // LinkedHashMap remembers the order things were inserted, so listings come
    // out in a predictable, human-friendly order.
    private final Map<String, Person> storage = new LinkedHashMap<>();

    @Override
    public Person save(Person person) {
        // put() adds a new entry or overwrites an existing one with the same id.
        storage.put(person.getId(), person);
        return person;
    }

    @Override
    public Optional<Person> findById(String id) {
        // Map.get returns null if the key is missing; Optional.ofNullable turns
        // that null into a clean, empty Optional for the caller.
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Person> findAll() {
        // Copy the values into a fresh list so outside code cannot secretly
        // modify our internal storage. Handing out your private collection
        // directly is a classic source of hard-to-find bugs.
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean deleteById(String id) {
        // remove() returns the removed value, or null if nothing was there.
        return storage.remove(id) != null;
    }

    @Override
    public long count() {
        return storage.size();
    }
}
