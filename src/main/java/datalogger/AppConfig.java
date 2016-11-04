package datalogger;

import datalogger.dao.EntryDao;
import datalogger.dao.EntrySql;
import datalogger.dao.EntryDaoJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    public static final String DRIVER_NAME = "org.h2.Driver";
    public static final String DATA_BASE = "jdbc:h2:/home/toss/workspace/dbs/data-logger";

    public DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER_NAME);
        dataSource.setUrl(DATA_BASE);
        dataSource.setUsername("");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate template = new JdbcTemplate(createDataSource());
        template.execute(EntrySql.CREATE_SCHEMA);
        return template;
    }

    @Bean
    public EntryDao createDao() {
        EntryDao entryDao = new EntryDaoJdbc();
        return entryDao;
    }
}
