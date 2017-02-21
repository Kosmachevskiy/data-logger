package datalogger.modbus;

import datalogger.modbus.configuration.DataLoggerConfiguration;
import datalogger.modbus.configuration.SerialSlave;
import datalogger.modbus.configuration.Source;
import datalogger.modbus.configuration.TcpSlave;
import datalogger.modbus.demo.FakeSlaveService;
import datalogger.model.dao.EntryDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Konstantin Kosmachevskiy
 */
public class ModbusPollerServiceIntegrationTest {

    @Mock
    private EntryDao entryDao;
    @InjectMocks
    private ModbusPollerService modbusPollerService;
    private DataLoggerConfiguration configuration;
    private FakeSlaveService fakeSlaveService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        configuration = new DataLoggerConfiguration();

        TcpSlave tcpSlave = new TcpSlave();
        tcpSlave.setId(10);
        tcpSlave.setPort(1502);
        tcpSlave.getSources().add(new Source("Door contact", "Open/Close", Source.Type.COIL, 100, 2));
        tcpSlave.getSources().add(new Source("Binary Sensor", "True/False", Source.Type.INPUT, 200, 2));
        tcpSlave.getSources().add(new Source("Brightness", "Lux", Source.Type.INPUT_REGISTER,
                300, 4, Source.DataType.FOUR_BYTE_INT_UNSIGNED));
        tcpSlave.getSources().add(new Source("Weight", "Kg", Source.Type.HOLDING_REGISTER,
                400, 5, Source.DataType.FOUR_BYTE_FLOAT));

        configuration.getTcpSlaves().add(tcpSlave);


        SerialSlave serialSlave = new SerialSlave();
        serialSlave.setId(1);
        serialSlave.getSources().add(new Source("(Serial) Door contact", "Open/Close", Source.Type.COIL, 100, 2));
        serialSlave.getSources().add(new Source("(Serial) Binary Sensor", "True/False", Source.Type.INPUT, 200, 2));
        serialSlave.getSources().add(new Source("(Serial) Brightness", "Lux", Source.Type.INPUT_REGISTER,
                300, 4, Source.DataType.FOUR_BYTE_INT_UNSIGNED));
        serialSlave.getSources().add(new Source("(Serial) Weight", "Kg", Source.Type.HOLDING_REGISTER,
                400, 5, Source.DataType.FOUR_BYTE_FLOAT));

        configuration.getSerialConfiguration().getSlaves().add(serialSlave);

        configuration.getSerialConfiguration().setPort(SerialPort.DEMO_SERIAL_PORT_POINT_A);

        fakeSlaveService = new FakeSlaveService(configuration);
        fakeSlaveService.startTcpSlaves();
        fakeSlaveService.startSerialSlaves(SerialPort.DEMO_SERIAL_PORT_POINT_B);

    }


    @Test
    public void youCanStartAndStopPolling() throws InterruptedException {


        modbusPollerService.start(configuration);

        //-- Wait a bit --//
        Thread.sleep(6_500);

        modbusPollerService.shutDown();


        verify(entryDao, times(16)).add(any());
    }

    @After
    public void tearDown() throws Exception {
        fakeSlaveService.stopTcpSlaves();
        fakeSlaveService.stopSerialSlaves();
    }
}
