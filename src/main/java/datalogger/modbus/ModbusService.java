package datalogger.modbus;

import datalogger.configuration.Source;

import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Kosmachevskiy
 */
public class ModbusService {


    /**
     * Puts sources into batches according to their polling interval
     *
     * @param batches
     * @param sources
     * @param slaveId
     */
    static void putSourcesIntoBatches(Map<Integer, datalogger.modbus.SourcesBatch> batches, List<Source> sources, int slaveId) {

        for (Source source : sources) {
            datalogger.modbus.SourcesBatch sourcesBatch;
            if (batches.containsKey(source.getPollingTime())) {
                sourcesBatch = batches.get(source.getPollingTime());
            } else {
                sourcesBatch = new datalogger.modbus.SourcesBatch();
                batches.put(source.getPollingTime(), sourcesBatch);
            }
            sourcesBatch.addSource(source, slaveId);
        }
    }
}
