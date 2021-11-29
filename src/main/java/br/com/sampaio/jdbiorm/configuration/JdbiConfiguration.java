package br.com.sampaio.jdbiorm.configuration;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class JdbiConfiguration {
    private final DataSource dataSource;

    public JdbiConfiguration(@Qualifier("dataSourceJdbi") final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean(name = "jdbi")
    @Primary
    public Jdbi jdbi() {
        return Jdbi.create(this.dataSource);
    }

}
