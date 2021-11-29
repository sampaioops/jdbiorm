package br.com.sampaio.jdbiorm;

import br.com.sampaio.jdbiorm.model.Person;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestCrudRepository extends AbstractTest {

    @Test
    void shouldSave() {
        final var sabrina = new Person(UUID.randomUUID(), "Sabrina");
        final var newEntity = repository.save(sabrina);

        assertNotNull(newEntity);
        assertEquals(sabrina.getId(), newEntity.getId());
        assertEquals(sabrina.getName(), newEntity.getName());
    }

    @Test
    void shouldFindByIdentifier() {
        final var identifier = UUID.randomUUID();
        final var wilson = new Person(identifier, "Wilson");
        repository.save(wilson);

        final var wilsonFound = repository.findByIdentifier(identifier);

        assertTrue(wilsonFound.isPresent());
        assertEquals(wilson.getId(), wilsonFound.get().getId());
        assertEquals(wilson.getName(), wilsonFound.get().getName());
    }

    @Test
    void shouldFindAll() {
        final var daniel = new Person(UUID.randomUUID(), "Daniel");
        repository.save(daniel);

        final var all = repository.findAll();

        assertFalse(all.isEmpty());
    }

    @Test
    void shouldDelete() {
        final var identifier = UUID.randomUUID();
        final var daniel = new Person(identifier, "Daniel");
        repository.save(daniel);

        repository.delete(identifier);

        final var danielFound = repository.findByIdentifier(identifier);

        assertTrue(danielFound.isEmpty());
    }

    @Test
    void shouldFindAllLikeNameWhenQueryPersonality() {
        final var harryPotter = new Person(UUID.randomUUID(), "Harry Potter");
        repository.save(harryPotter);

        final var initName = harryPotter.getName().substring(0, 5);

        final var allEntitiesWithInitName = repository.findAllLikeName(initName);

        assertFalse(allEntitiesWithInitName.isEmpty());

        final var harryPotterFound = allEntitiesWithInitName
                .stream()
                .filter(person -> person.getName().equals(harryPotter.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(harryPotterFound);
    }
}
