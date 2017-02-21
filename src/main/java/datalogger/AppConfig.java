package datalogger;

import datalogger.modbus.ConfigurationService;
import datalogger.modbus.ModbusPollerService;
import datalogger.modbus.SerialPort;
import datalogger.modbus.configuration.DataLoggerConfiguration;
import datalogger.modbus.demo.FakeSlaveService;
import datalogger.model.dao.EntryDao;
import datalogger.model.dao.EntryDaoJdbc;
import datalogger.services.ReportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    @Value("${db.driver}")
    public String driverName;
    @Value("${db.url}")
    public String url;
    @Value("${app.home}")
    public String homeFolder;

    private static EntryDao getEntryDao(String driverName, String url) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl(url);
        EntryDaoJdbc entryDaoJdbc = new EntryDaoJdbc();
        entryDaoJdbc.setDataSource(dataSource);
        entryDaoJdbc.getJdbcTemplate().execute(DB_SCHEMA);
        return entryDaoJdbc;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer
    propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(value = "entryDao", autowire = Autowire.BY_NAME)
    @Profile("production")
    public EntryDao trueEntryDao() {
        return getEntryDao(driverName, url);
    }

    @Bean(name = "entryDao", autowire = Autowire.BY_NAME)
    @Profile("demo")
    public EntryDao demoEntryDao() {
        EntryDao dao = getEntryDao(driverName, url + "-demo");
        return dao;
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
    public ModbusPollerService modbusPollerService() {
        return new ModbusPollerService();
    }

    @PreDestroy
    public void shutdown() {
        modbusPollerService().shutDown();
    }

    @Bean
    @Profile("demo")
    public FakeSlaveService fakeSlaveService() {
        DataLoggerConfiguration configuration = DataLoggerConfiguration.createDemoConfig();
        configuration.getTcpSlaves().get(0).setPort(1502);
        configuration.getSerialConfiguration().setPort(SerialPort.DEMO_SERIAL_PORT_POINT_A);

        return new FakeSlaveService(configuration);
    }

    @Bean
    @Profile("demo")
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    @Profile("demo")
    public void demo() {

        fakeSlaveService().startTcpSlaves();
        fakeSlaveService().startSerialSlaves(SerialPort.DEMO_SERIAL_PORT_POINT_B);

        //-- Run Pollers with the same config as Slaves --//
        modbusPollerService().start(fakeSlaveService().getConfiguration());

        demoEntryDao().deleteAll();

        scheduledExecutorService().scheduleAtFixedRate(() -> {
            if (demoEntryDao().countEntries() > 400)
                demoEntryDao().deleteAll();
        }, 1, 1, TimeUnit.MINUTES);
    }

    @PreDestroy
    @Profile("demo")
    public void demoDestroy() {
        fakeSlaveService().stopSerialSlaves();
        fakeSlaveService().stopTcpSlaves();
        scheduledExecutorService().shutdownNow();
    }
}