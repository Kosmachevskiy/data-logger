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
@PropertySources({
        @PropertySource("classpath:app.properties"),
        @PropertySource("classpath:db.properties")
})
public class AppConfig {

    private static final String DB_SCHEMA =
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

    private DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl(url);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public EntryDao entryDao() {
        return new EntryDaoJdbc();
    }

    @Bean
    public ConfigurationService configurationService() {
        return new ConfigurationService(homeFolder);
    }

    @Bean
    public ReportBuilder reportBuilder() {
        return new ReportBuilder();
    }

    @PostConstruct
    public void init() {
        jdbcTemplate().execute(DB_SCHEMA);
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
