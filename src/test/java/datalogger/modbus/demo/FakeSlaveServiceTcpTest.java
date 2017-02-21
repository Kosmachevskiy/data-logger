package datalogger.modbus.demo;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.IpParameters;
import datalogger.modbus.configuration.DataLoggerConfiguration;
import datalogger.modbus.configuration.TcpSlave;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Konstantin Kosmachevskiy
 */
public class FakeSlaveServiceTcpTest {

    private static final int MODBUS_TCP_PORT = 1502;
    private static final String MODBUS_TCP_HOST = "127.0.0.1";
    private IpParameters parameters1;
    private IpParameters parameters2;
    private DataLoggerConfiguration configuration;
    private ModbusMaster master1;
    private ModbusMaster master2;

    @Before
    public void prepareConfig() {
        configuration = DataLoggerConfiguration.createDemoConfig();
        configuration.getTcpSlaves().get(0).setPort(MODBUS_TCP_PORT);

        configuration.getTcpSlaves().add(new TcpSlave());
        configuration.getTcpSlaves().get(1).setId(4);
        configuration.getTcpSlaves().get(1).setPort(MODBUS_TCP_PORT + 1);
        configuration.getTcpSlaves().get(1).setSources(configuration.getTcpSlaves().get(0).getSources());


        parameters1 = new IpParameters();
        parameters1.setPort(configuration.getTcpSlaves().get(0).getPort());
        parameters1.setHost(MODBUS_TCP_HOST);

        parameters2 = new IpParameters();
        parameters2.setPort(configuration.getTcpSlaves().get(1).getPort());
        parameters2.setHost(MODBUS_TCP_HOST);

        ModbusFactory modbusFactory = new ModbusFactory();
        master1 = modbusFactory.createTcpMaster(parameters1, false);
        master2 = modbusFactory.createTcpMaster(parameters2, false);

        try {
            master1.init();
            master2.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void startAndStopTcpSlave() throws Exception {

        FakeSlaveService fakeSlaveService = new FakeSlaveService(configuration);

        // -- Check Masters: Slaves #12 are not exit --//
        Assert.assertFalse(master1.testSlaveNode(12));
        Assert.assertFalse(master2.testSlaveNode(12));

        fakeSlaveService.startTcpSlaves();
        Assert.assertTrue(master1.testSlaveNode(configuration.getTcpSlaves().get(0).getId()));
        Assert.assertTrue(master2.testSlaveNode(configuration.getTcpSlaves().get(1).getId()));

        fakeSlaveService.stopTcpSlaves();
        Assert.assertFalse(master1.testSlaveNode(configuration.getTcpSlaves().get(0).getId()));
        Assert.assertFalse(master2.testSlaveNode(configuration.getTcpSlaves().get(1).getId()));

    }

    @After
    public void destroyTestDemoMasters() {
        master1.destroy();
        master2.destroy();
    }
}