package br.com.sampaio.jdbiorm;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<E, I> {

    List<E> findAll();
    E save(final E entity);
    Optional<E> findByIdentifier(final I identifier);
    void delete(final I identifier);

}
