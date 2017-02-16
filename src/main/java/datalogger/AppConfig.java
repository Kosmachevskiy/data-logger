package datalogger;

import datalogger.modbus.ConfigurationService;
import datalogger.modbus.ModbusPollerService;
import datalogger.model.Entry;
import datalogger.model.dao.EntryDao;
import datalogger.model.dao.EntryDaoJdbc;
import datalogger.services.ReportBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan(basePackages = "datalogger")
@PropertySources({
        @PropertySource("classpath:app.properties"),
        @PropertySource("classpath:db.properties")
})
public class AppConfig {

    public static final String DB_SCHEMA =
            "CREATE TABLE IF NOT EXISTS entries (id identity, date DATE, time TIME, " +
                    "name VARCHAR (50) ,value VARCHAR(255), unit VARCHAR(20));";

    @Value("${db.driver}")
    public String driverName;
    @Value("${db.url}")
    public String url;
    @Value("${app.home}")
    public String homeFolder;

    @Bean
    public static PropertySourcesPlaceholderConfigurer
    propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Bean
    @Profile("production")
    public EntryDao entryDao() {
        return getEntryDao(driverName, url);
    }

    @Bean
    @Profile("demo")
    public EntryDao demoEntryDao() {
        return getEntryDao(driverName, url + "-demo");
    }

    private EntryDao getEntryDao(String driverName, String url) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl(url);
        EntryDaoJdbc entryDaoJdbc = new EntryDaoJdbc();
        entryDaoJdbc.setDataSource(dataSource);
        entryDaoJdbc.getJdbcTemplate().execute(DB_SCHEMA);
        return entryDaoJdbc;
    }

    @Bean
    public ConfigurationService configurationService() {
        return new ConfigurationService(homeFolder);
    }

    @Bean
    public ReportBuilder reportBuilder() {
        return new ReportBuilder();
    }

    @Bean
    @Profile("production")
    public ModbusPollerService modbusPollerService() {
        ModbusPollerService service = new ModbusPollerService();
        service.start(configurationService().load());
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
        public void init() {
            entryDao.deleteAll();

            service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(() -> {
                if (entryDao.countEntries() >= 1000)
                    entryDao.deleteAll();
                Random random = new Random(System.currentTimeMillis());
                entryDao.add(new Entry(
                        "Source " + (random.nextInt(NUMBER_OF_SOURCES) + 1),
                        String.valueOf((random.nextInt(99) + 1)),
                        "Unit"));
            }, POLLING_TIME, POLLING_TIME, TimeUnit.SECONDS);
        }

        @PreDestroy
        public void destroy() {
            if (service != null)
                service.shutdownNow();
        }
    }
}