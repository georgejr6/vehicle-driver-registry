package gov.dmv.registry.repository;

import gov.dmv.registry.model.Person;

import java.util.List;
import java.util.Optional;

/**
 * A "repository" is the layer responsible for STORING and RETRIEVING objects.
 * Think of it as the filing cabinet for one kind of thing - here, People.
 *
 * WHY THIS IS AN INTERFACE (and not a class):
 * An interface lists WHAT operations exist without saying HOW they work. It is
 * a promise / a contract. Today we back it with a simple in-memory version
 * (data lives in the computer's memory and disappears when the app stops).
 * Later, when we "modernise", we can write a database-backed version that
 * fulfils the exact same contract - and NONE of our business code has to change,
 * because it only ever talks to this interface. This swap-ability is the single
 * most important idea in this whole project.
 */
public interface PersonRepository {

    /** Store a new person (or update an existing one) and return it. */
    Person save(Person person);

    /**
     * Look up a person by id. Returns an Optional - Java's official way of
     * saying "maybe there's a Person here, maybe there isn't". It forces the
     * caller to handle the "not found" case instead of crashing on null.
     */
    Optional<Person> findById(String id);

    /** Return every person we know about (as a read-only-style list). */
    List<Person> findAll();

    /** Remove a person by id. Returns true if someone was actually removed. */
    boolean deleteById(String id);

    /** How many people are stored - handy for generating the next id. */
    long count();
}
