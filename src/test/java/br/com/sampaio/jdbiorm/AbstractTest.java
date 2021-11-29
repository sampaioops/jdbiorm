package br.com.sampaio.jdbiorm;

import br.com.sampaio.jdbiorm.repository.PersonRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;

public abstract class AbstractTest {

    protected PersonRepository repository;

    @BeforeEach
    void setup(){
        final var jdbi = getJdbi();
        this.repository = new PersonRepository(jdbi);
    }

    private Jdbi getJdbi() {
        final DataSource dataSource = getDataSource();
        return Jdbi.create(dataSource);
    }

    private DataSource getDataSource() {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:postgresql://localhost:5432/dev");
        hikariConfig.setUsername("postgres");
        hikariConfig.setPassword("postgres");
        hikariConfig.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(hikariConfig);
    }
}
