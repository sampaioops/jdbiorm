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

public abstract class AbstractRepository<T, I> {

    protected final Jdbi jdbi;
    private final Class<T> genericType;

    protected AbstractRepository(final Jdbi jdbi) {
        this.genericType = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.jdbi = jdbi;
    }

    public List<T> findAll() {
        final String tableName = getTableName();

        final var statement = format("SELECT * FROM %s", tableName);

        return jdbi.withHandle(handle -> handle.select(statement)
                .map((rs, ctx) -> {
                    try {
                        return getEntity(rs);
                    } catch (final InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).list()
        );
    }

    public T save(final T entity) {
        final String tableName = getTableName();
        final HashMap<String, Object> columnsAndValues = getColumnsAndValues(entity);

        final var columnNames = columnsAndValues.keySet()
                .stream()
                .collect(Collectors.joining(",", "", ""));

        //TODO: Create statement values per field type
        final var values = columnsAndValues.values()
                .stream()
                .map(o -> format("'%s'", o.toString()))
                .collect(Collectors.joining(","));

        final String statement = format("INSERT INTO %s (%s) VALUES (%s)", tableName, columnNames, values);

        return jdbi.withHandle(handle -> handle.createUpdate(statement)
                .executeAndReturnGeneratedKeys()
                .map((rs, ctx) -> {
                    try {
                        return getEntity(rs);
                    } catch (final InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).one()
        );
    }

    public Optional<T> findByIdentifier(final I identifier) {
        final var tableName = getTableName();
        final var identifierColumnName = getIdentifierColumnName();

        final var statement = format("SELECT * FROM %s WHERE %s = :%s", tableName, identifierColumnName, identifierColumnName);

        return jdbi.withHandle(handle -> handle.select(statement)
                .bind(identifierColumnName, identifier)
                .map((rs, ctx) -> {
                    try {
                        return getEntity(rs);
                    } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).findOne()
        );
    }

    public void delete(final I identifier) {
        final var tableName = getTableName();
        final var identifierName = getIdentifierColumnName();

        final var statement = format("DELETE FROM %s WHERE %s = :%s", tableName, identifierName, identifierName);

        jdbi.withHandle(handle -> handle.createUpdate(statement)
                .bind(identifierName, identifier)
                .execute()
        );
    }

    private String getTableName() {
        final Table table = genericType.getAnnotation(Table.class);
        return table.name();
    }

    private String getIdentifierColumnName() {
        return Arrays.stream(genericType.getFields())
                .filter(field -> field.getAnnotation(Id.class) != null)
                .findFirst()
                .map(field -> field.getAnnotation(Column.class).name())
                .orElse("id");
    }

    private T getEntity(final ResultSet rs) throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, InvocationTargetException {
        final T entity = genericType.getDeclaredConstructor().newInstance();

        final Field[] fields = entity.getClass().getDeclaredFields();

        for (final Field field : fields) {
            field.setAccessible(true);

            final Column annotation = field.getAnnotation(Column.class);

            final String column = annotation.name();

            final var value = rs.getObject(column, field.getType());

            field.set(entity, value);
        }

        return entity;
    }

    private HashMap<String, Object> getColumnsAndValues(final T entity) {
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

}
