package br.com.sampaio.jdbiorm;

import br.com.sampaio.jdbiorm.AbstractTest;
import br.com.sampaio.jdbiorm.model.Person;
import org.junit.jupiter.api.Test;

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
}
