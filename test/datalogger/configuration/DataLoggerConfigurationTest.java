package datalogger.configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Konstantin Kosmachevskiy
 */
public class DataLoggerConfigurationTest {
    @Test
    public void saveAndLoad() throws Exception {
        DataLoggerConfiguration configuration = new DataLoggerConfiguration();

        // Source creation. We can put it in any slave node.
        Source source = new Source();
        source.setType(Source.Type.COIL);
        source.setName("SomeName");
        source.setAddress(999);
        source.setDataType(Source.DataType.FOUR_BYTE_FLOAT);

        // Serial Slave nodes creation and add source
        SerialSlave serialSlave1 = new SerialSlave();
        serialSlave1.setId(1);
        SerialSlave serialSlave2 = new SerialSlave();
        serialSlave2.setId(2);
        // Two sources into first slave node and Three into second
        serialSlave1.getSources().add(source);
        serialSlave1.getSources().add(source);
        serialSlave2.getSources().add(source);
        serialSlave2.getSources().add(source);
        serialSlave2.getSources().add(source);

        // Put node into configuration
        configuration.getSerialConfiguration().getSlaves().add(serialSlave1);
        configuration.getSerialConfiguration().getSlaves().add(serialSlave2);


        // And the same actions for TCP configuration
        TcpSlave tcpSlave1 = new TcpSlave();
        tcpSlave1.setId(1);
        tcpSlave1.setHost("192.168.1.201");
        tcpSlave1.setPort(502);
        TcpSlave tcpSlave2 = new TcpSlave();
        tcpSlave2.setId(2);
        tcpSlave2.setHost("192.168.1.202");
        tcpSlave2.setPort(502);
        tcpSlave1.getSources().add(source);
        tcpSlave1.getSources().add(source);
        tcpSlave2.getSources().add(source);
        tcpSlave2.getSources().add(source);
        tcpSlave2.getSources().add(source);
        configuration.getTcpSlaves().add(tcpSlave1);
        configuration.getTcpSlaves().add(tcpSlave2);

        Assert.assertTrue(DataLoggerConfiguration.save(configuration));
        Assert.assertEquals(DataLoggerConfiguration.load(), configuration);
    }

    @Test
    public void defaultConfig(){
        // There are four sources in Demo configuration
        Assert.assertEquals(4, DataLoggerConfiguration
                .createDemoConfig().getTcpSlaves().get(0).getSources().size());
        Assert.assertEquals(4, DataLoggerConfiguration
                .createDemoConfig().getSerialConfiguration().getSlaves().get(0).getSources().size());

        DataLoggerConfiguration.save(DataLoggerConfiguration.createDemoConfig());

    }

}