package datalogger.modbus;

import datalogger.modbus.configuration.Source;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Kosmachevskiy
 */
public class ModbusPollerServiceTest {

    @Test
    public void groupSourcesByInterval() throws Exception {

        List<Source> sources = new ArrayList<>();
        int slaveId = 100;

        sources.add(new Source("SomeName1", "Unit", Source.Type.INPUT, 100, 1000));
        sources.add(new Source("SomeName3", "Unit",
                Source.Type.HOLDING_REGISTER, 300, 1500, Source.DataType.EIGHT_BYTE_INT_SIGNED));
        sources.add(new Source("SomeName2", "Unit", Source.Type.COIL, 200, 1000));
        sources.add(new Source("SomeName4", "Unit",
                Source.Type.INPUT_REGISTER, 400, 700, Source.DataType.EIGHT_BYTE_INT_SIGNED));


        Map<Integer, SourcesBatch> map = ModbusPollerService.groupSourcesByInterval(sources, slaveId);

        // Map must contains 3 Batches
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(2, map.get(1000).getSources().size());
        Assert.assertEquals(1, map.get(1500).getSources().size());
        Assert.assertEquals(1, map.get(700).getSources().size());
    }

}