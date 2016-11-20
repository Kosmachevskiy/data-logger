package datalogger;

import datalogger.dao.EntryDao;
import datalogger.dao.EntryDaoJdbc;
import datalogger.dao.EntrySqlConstants;
import datalogger.model.Entry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class AppConfig {

    public static final String DRIVER_NAME = "org.h2.Driver";
    public static final String DATA_BASE = "jdbc:h2:/home/toss/workspace/dbs/data-logger";

    private DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER_NAME);
        dataSource.setUrl(DATA_BASE);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate template = new JdbcTemplate(createDataSource());
        template.execute(EntrySqlConstants.CREATE_SCHEMA);
        return template;
    }

    @Bean
    public EntryDao getDao() {
        EntryDao entryDao = new EntryDaoJdbc();
        return entryDao;
    }

    @PostConstruct
    public void testData() {
        EntryDao dao = getDao();
        dao.deleteAll();
        for (int i = 0; i < 1000; i++) {
            Entry entry = new Entry("Value " + i, "Unit");
            dao.add(entry);
        }
    }

}
