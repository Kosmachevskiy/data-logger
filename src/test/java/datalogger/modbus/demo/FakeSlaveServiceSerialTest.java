package datalogger.modbus.demo;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import datalogger.modbus.SerialPort;
import datalogger.modbus.configuration.DataLoggerConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Konstantin Kosmachevskiy
 */
public class FakeSlaveServiceSerialTest {
    private static final String MASTER_SERIAL_PORT = "/dev/ttyUSB0";
    private static final String SLAVE_SERIAL_PORT = "/dev/ttyUSB1";

    private DataLoggerConfiguration configuration;
    private ModbusFactory factory = new ModbusFactory();
    private ModbusMaster modbusMaster;

    @Before
    public void setUp() throws Exception {
        configuration = DataLoggerConfiguration.createDemoConfig();
        configuration.getTcpSlaves().clear();

        configuration.getSerialConfiguration().setPort(SerialPort.DEMO_SERIAL_PORT_POINT_A);
        modbusMaster = factory.createRtuMaster(SerialPort.getWrapper(configuration.getSerialConfiguration()));
        modbusMaster.init();
    }

    @Test
    public void startAndStop() throws Exception {
        FakeSlaveService fakeSlaveService = new FakeSlaveService(configuration);

        Assert.assertFalse(modbusMaster.testSlaveNode(configuration.getSerialConfiguration().getSlaves().get(0).getId()));

        fakeSlaveService.startSerialSlaves(SerialPort.DEMO_SERIAL_PORT_POINT_B);
        Assert.assertTrue(modbusMaster.testSlaveNode(configuration.getSerialConfiguration().getSlaves().get(0).getId()));
        fakeSlaveService.stopSerialSlaves();
        Assert.assertFalse(modbusMaster.testSlaveNode(configuration.getSerialConfiguration().getSlaves().get(0).getId()));

    }

    @After
    public void tearDown() throws Exception {
        modbusMaster.destroy();
    }
}