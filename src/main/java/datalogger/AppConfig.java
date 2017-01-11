package datalogger;

import datalogger.dao.EntryDao;
import datalogger.dao.EntryDaoJdbc;
import datalogger.model.Entry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "datalogger")
public class AppConfig {

    public static final String DRIVER_NAME = "org.h2.Driver";
    public static final String DATA_BASE = "jdbc:h2:./data-logger-database";
    public static final String DB_SCHEMA =
            "CREATE TABLE IF NOT EXISTS entries (id identity, date DATE, time TIME, " +
                    "name VARCHAR (50) ,value VARCHAR(255), unit VARCHAR(20));";

    private DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER_NAME);
        dataSource.setUrl(DATA_BASE);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate template = new JdbcTemplate(createDataSource());
        return template;
    }

    @Bean
    public EntryDao getDao() {
        EntryDao entryDao = new EntryDaoJdbc();
        return entryDao;
    }

    @PostConstruct
    public void initDatabaseAndDemoData() {
        jdbcTemplate().execute(DB_SCHEMA);
        EntryDao dao = getDao();
        dao.deleteAll();
        for (int i = 0; i < 10; i++) {
            Entry entry = new Entry("Sourse" + i,"Value " + i, "Unit");
            dao.add(entry);
        }
    }

}
