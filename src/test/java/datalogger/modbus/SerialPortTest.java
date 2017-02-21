package datalogger.modbus;

import com.serotonin.modbus4j.BasicProcessImage;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ModbusSlaveSet;
import com.serotonin.modbus4j.exception.ModbusInitException;
import datalogger.modbus.configuration.DataLoggerConfiguration;
import datalogger.modbus.configuration.SerialConfiguration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Konstantin Kosmachevskiy
 */
public class SerialPortTest {
    private static BasicProcessImage buildProcessImage(int slaveId) {
        BasicProcessImage processImage = new BasicProcessImage(slaveId);
        processImage.setAllowInvalidAddress(true);
        processImage.setInvalidAddressValue(Short.MIN_VALUE);
        processImage.setExceptionStatus((byte) 151);

        return processImage;
    }

    @Test
    @Ignore
    public void realSerialTest() throws Exception {
        test("/dev/ttyUSB0", "/dev/ttyUSB1");
    }

    @Test
    public void demoSerialTest() throws Exception {
        test(SerialPort.DEMO_SERIAL_PORT_POINT_A, SerialPort.DEMO_SERIAL_PORT_POINT_B);
    }

    private void test(String masterPort, String slavePort) throws Exception {
        SerialConfiguration configurationMaster = DataLoggerConfiguration.createDemoConfig().getSerialConfiguration();
        SerialConfiguration configurationSlave = DataLoggerConfiguration.createDemoConfig().getSerialConfiguration();
        ModbusFactory modbusFactory = new ModbusFactory();

        configurationMaster.setPort(masterPort);
        configurationSlave.setPort(slavePort);

        ModbusMaster master = modbusFactory.createRtuMaster(SerialPort.getWrapper(configurationMaster));
        ModbusSlaveSet slave = modbusFactory.createRtuSlave(SerialPort.getWrapper(configurationSlave));

        slave.addProcessImage(buildProcessImage(15));

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                slave.start();
            } catch (ModbusInitException e) {
                e.printStackTrace();
            }
        });

        master.init();
        Assert.assertTrue(master.testSlaveNode(15));
        Assert.assertFalse(master.testSlaveNode(14));
        slave.stop();
        Assert.assertFalse(master.testSlaveNode(15));
        master.destroy();
    }

}