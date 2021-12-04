package br.com.sampaio.jdbiorm.repository;

import br.com.sampaio.jdbiorm.AbstractRepository;
import br.com.sampaio.jdbiorm.model.Person;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

public class PersonRepository extends AbstractRepository<Person, UUID> {

    public PersonRepository(final Jdbi jdbi) {
        super(jdbi);
    }

    public List<Person> findAllLikeByName(final String name) {
        final var tableName = getTableName();
        final var statement = format("SELECT * FROM %s WHERE name LIKE :name;", tableName);

        return super.jdbi.withHandle(handle -> handle.select(statement)
                .bind("name", "%" + name + "%")
                .map((rs, ctx) -> getEntity(rs))
                .list());
    }
}
