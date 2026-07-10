package gov.dmv.registry.repository;

import gov.dmv.registry.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The storage layer for People - now powered by Spring Data JPA.
 *
 * THE BIG "MODERNIZE" MOMENT:
 * Before, we had to hand-write an InMemoryPersonRepository with save/findById/
 * findAll/etc. Now we simply DECLARE this interface and extend
 * {@code JpaRepository<Person, Long>}. Spring generates a full, database-backed
 * implementation at startup - we write ZERO storage code.
 *
 * The two type parameters mean: this repository stores {@link Person} objects
 * whose primary key (id) is a {@link Long}.
 *
 * For free, we instantly get methods like:
 *   save(person), findById(id), findAll(), deleteById(id), count(), and more.
 *
 * We can also add custom look-ups just by naming a method a certain way (Spring
 * writes the query from the name) - see VehicleRepository for examples. Person
 * needs none yet, so this interface stays empty. That is not laziness; it is the
 * framework doing the boring work so we focus on the interesting rules.
 */
public interface PersonRepository extends JpaRepository<Person, Long> {
}
