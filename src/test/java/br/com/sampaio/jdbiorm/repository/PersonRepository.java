package br.com.sampaio.jdbiorm.repository;

import br.com.sampaio.jdbiorm.AbstractRepository;
import br.com.sampaio.jdbiorm.model.Person;
import org.jdbi.v3.core.Jdbi;

import java.util.UUID;

public class PersonRepository extends AbstractRepository<Person, UUID> {

    public PersonRepository(final Jdbi jdbi) {
        super(jdbi);
    }
}
