package datalogger.modbus;

import datalogger.configuration.DataLoggerConfiguration;
import datalogger.configuration.SerialSlave;
import datalogger.configuration.Source;
import datalogger.configuration.TcpSlave;
import datalogger.dao.EntryDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Kosmachevskiy
 */
public class ModbusServiceTest {

    @Mock
    private EntryDao entryDao;
    @InjectMocks
    private ModbusService modbusService;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void putSourcesIntoBatches() throws Exception {

        List<Source> sources = new ArrayList<>();
        int slaveId = 100;

        sources.add(new Source("SomeName1", "Unit", Source.Type.INPUT, 100, 1000));
        sources.add(new Source("SomeName3", "Unit",
                Source.Type.HOLDING_REGISTER, 300, 1500, Source.DataType.EIGHT_BYTE_INT_SIGNED));
        sources.add(new Source("SomeName2", "Unit", Source.Type.COIL, 200, 1000));
        sources.add(new Source("SomeName4", "Unit",
                Source.Type.INPUT_REGISTER, 400, 700, Source.DataType.EIGHT_BYTE_INT_SIGNED));


        Map<Integer, SourcesBatch> map = modbusService.groupSourcesByInterval(sources, slaveId);

        // Map must contains a 3 Batches
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(2, map.get(1000).getSources().size());
        Assert.assertEquals(1, map.get(1500).getSources().size());
        Assert.assertEquals(1, map.get(700).getSources().size());
    }

    @Test
    @Ignore // TODO: make this test independent
    public void youCanStartAndStopPolling() throws InterruptedException {
        // Setup //
        DataLoggerConfiguration configuration = new DataLoggerConfiguration();

        TcpSlave tcpSlave = new TcpSlave();
        tcpSlave.setId(1);
        tcpSlave.getSources().add(new Source("Door contact", "Open/Close", Source.Type.COIL, 100, 2));
        tcpSlave.getSources().add(new Source("Binary Sensor", "True/False", Source.Type.INPUT, 200, 2));
        tcpSlave.getSources().add(new Source("Brightness", "Lux", Source.Type.INPUT_REGISTER,
                300, 4, Source.DataType.FOUR_BYTE_INT_UNSIGNED));
        tcpSlave.getSources().add(new Source("Weight", "Kg", Source.Type.HOLDING_REGISTER,
                400, 5, Source.DataType.FOUR_BYTE_FLOAT));

        SerialSlave serialSlave = new SerialSlave();
        serialSlave.setId(1);
        serialSlave.setSources(tcpSlave.getSources());

        configuration.getTcpSlaves().add(tcpSlave);
        DataLoggerConfiguration.save(configuration);

        modbusService.start();
        Thread.sleep(6_500);
        modbusService.shutDown();

//        verify(entryDao, times(8)).add(any());
    }

}