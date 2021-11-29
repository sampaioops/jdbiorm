package br.com.sampaio.jdbiorm.model;

import br.com.sampaio.jdbiorm.annotation.Column;
import br.com.sampaio.jdbiorm.annotation.Id;
import br.com.sampaio.jdbiorm.annotation.Table;

import java.util.UUID;

@Table(name = "entity_test")
public class Person {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    //NOTE: This is necessary for generate a new instance with reflections
    public Person() {
    }

    public Person(final UUID id,
                  final String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + "\"" + this.id + "\"," +
                "name:" + "\"" + this.name + "\"" +
                "}";
    }
}
