package br.com.sampaio.jdbiorm;

import br.com.sampaio.jdbiorm.annotation.Column;
import br.com.sampaio.jdbiorm.annotation.Id;
import br.com.sampaio.jdbiorm.annotation.Table;
import org.jdbi.v3.core.Jdbi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

public abstract class AbstractRepository<E, I> implements CrudRepository<E, I>{

    protected final Jdbi jdbi;
    private final Class<E> genericType;

    protected AbstractRepository(final Jdbi jdbi) {
        this.genericType = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.jdbi = jdbi;
    }

    @Override
    public List<E> findAll() {
        final String tableName = getTableName();

        final var statement = format("SELECT * FROM %s", tableName);

        return jdbi.withHandle(handle -> handle.select(statement)
                .map((rs, ctx) -> getEntity(rs))
                .list()
        );
    }

    @Override
    public E save(final E entity) {
        final String tableName = getTableName();
        final HashMap<String, Object> columnsAndValues = getColumnsAndValues(entity);

        final var bindColumns = columnsAndValues.keySet()
                .stream()
                .collect(Collectors.joining(",", "", ""));

        final var bindValues = columnsAndValues.keySet()
                .stream()
                .map(bindValue -> format(":%s", bindValue))
                .collect(Collectors.joining(",", "", ""));

        final String statement = format("INSERT INTO %s (%s) VALUES (%s)", tableName, bindColumns, bindValues);

        return jdbi.withHandle(handle -> handle.createUpdate(statement)
                .bindMap(columnsAndValues)
                .executeAndReturnGeneratedKeys()
                .map((rs, ctx) -> getEntity(rs))
                .one()
        );
    }

    @Override
    public Optional<E> findByIdentifier(final I identifier) {
        final var tableName = getTableName();
        final var identifierColumnName = getIdentifierColumnName();

        final var statement = format("SELECT * FROM %s WHERE %s = :%s", tableName, identifierColumnName, identifierColumnName);

        return jdbi.withHandle(handle -> handle.select(statement)
                .bind(identifierColumnName, identifier)
                .map((rs, ctx) -> getEntity(rs))
                .findOne()
        );
    }

    @Override
    public void delete(final I identifier) {
        final var tableName = getTableName();
        final var identifierName = getIdentifierColumnName();

        final var statement = format("DELETE FROM %s WHERE %s = :%s", tableName, identifierName, identifierName);

        jdbi.withHandle(handle -> handle.createUpdate(statement)
                .bind(identifierName, identifier)
                .execute()
        );
    }

    protected String getTableName() {
        final Table table = genericType.getAnnotation(Table.class);
        return table.name();
    }

    protected String getIdentifierColumnName() {
        return Arrays.stream(genericType.getDeclaredFields())
                .filter(field -> field.getAnnotation(Id.class) != null)
                .findFirst()
                .map(field -> field.getAnnotation(Column.class).name())
                .orElse("id");
    }

    protected E getEntity(final ResultSet rs) {
        try {
            final E entity = genericType.getDeclaredConstructor().newInstance();

            final Field[] declaredFields = entity.getClass().getDeclaredFields();

            Arrays.stream(declaredFields)
                    .forEach(field -> filledField(rs, field, entity));

            return entity;
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected HashMap<String, Object> getColumnsAndValues(final E entity) {
        final HashMap<String, Object> keyAndValues = new HashMap<>();

        final Field[] fields = entity.getClass().getDeclaredFields();

        for (final Field field : fields) {
            field.setAccessible(true);
            final Column column = field.getAnnotation(Column.class);

            final String columnName = column.name().equals("") ? field.getName() : column.name();
            try {
                keyAndValues.put(columnName, field.get(entity));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return keyAndValues;
    }

    private void filledField(final ResultSet resultSet,
                             final Field field,
                             final E entityTarget) {
        field.setAccessible(true);

        final Column annotation = field.getAnnotation(Column.class);

        final String column = annotation.name();

        try {
            final var value = resultSet.getObject(column, field.getType());
            field.set(entityTarget, value);
        } catch (final IllegalAccessException | SQLException e) {
            e.printStackTrace();
        }
    }

}
