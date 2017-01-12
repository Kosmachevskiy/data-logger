package datalogger;

import datalogger.configuration.DataLoggerConfiguration;
import datalogger.dao.EntryDao;
import datalogger.dao.EntryDaoJdbc;
import datalogger.modbus.ModbusService;
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
    private static final String DB_SCHEMA =
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
        return new JdbcTemplate(createDataSource());
    }

    @Bean
    public EntryDao getDao() {
        return new EntryDaoJdbc();
    }

    @Bean
    public ModbusService modbusService() {
        ModbusService service = new ModbusService();
//        service.start(); //TODO: uncomment this before release
        return service;
    }

    @PostConstruct // TODO: make this optional
    public void initDatabaseAndDemoData() {
        // Prepare DB //
        jdbcTemplate().execute(DB_SCHEMA);
        EntryDao dao = getDao();
        dao.deleteAll();
        // Fill  DB //
        for (int i = 0; i < 10; i++) {
            Entry entry = new Entry("Source" + i, "Value " + i, "Unit");
            dao.add(entry);
        }
        // Reset config and start polling //
        DataLoggerConfiguration.save(DataLoggerConfiguration.createDemoConfig());
        modbusService().start();
    }

}
