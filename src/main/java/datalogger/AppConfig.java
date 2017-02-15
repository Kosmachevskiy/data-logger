package datalogger;

import datalogger.dao.EntryDao;
import datalogger.dao.EntryDaoJdbc;
import datalogger.modbus.ModbusService;
import datalogger.model.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    public EntryDao entryDao() {
        return new EntryDaoJdbc();
    }

    @PostConstruct
    public void init(){
        jdbcTemplate().execute(DB_SCHEMA);
    }

    @Bean
    @Profile("production")
    public ModbusService modbusService() {
        ModbusService service = new ModbusService();
        service.start();
        return service;
    }

    @Service
    @Profile("demo")
    static class FakePoller {

        private static final long POLLING_TIME = 1;
        private static final int NUMBER_OF_SOURCES = 4;
        private ScheduledExecutorService service;
        @Autowired
        private EntryDao entryDao;

        @PostConstruct
        public void init(){
            entryDao.deleteAll();

            service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(() -> {
                Random random = new Random(System.currentTimeMillis());
                entryDao.add(new Entry(
                        "Source " + (random.nextInt(NUMBER_OF_SOURCES)+1),
                        String.valueOf((random.nextInt(99)+1)),
                        "Unit"));
            }, POLLING_TIME, POLLING_TIME, TimeUnit.SECONDS);
        }

        @PreDestroy
        public void destroy(){
            if (service!=null)
                service.shutdownNow();
        }
    }
}
