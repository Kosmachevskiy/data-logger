package datalogger.modbus;

import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import datalogger.modbus.configuration.*;
import datalogger.model.Entry;
import datalogger.model.dao.EntryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Konstantin Kosmachevskiy
 */
public class ModbusPollerService {

    private static final Logger logger = LoggerFactory.getLogger(ModbusPollerService.class);
    private ModbusFactory modbusFactory;
    private ScheduledExecutorService pollerExecutorService;
    @Autowired
    @Qualifier("entryDao")
    private EntryDao entryDao;
    private List<ModbusMaster> masters = new ArrayList<>();

    {   // TODO: is it work as I expect?
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.debug("Shutdown Hook.");
            shutDown();
        }));
    }

    /**
     * Groups Sources by polling interval
     */
    static Map<Integer, SourcesBatch> groupSourcesByInterval(List<Source> sources, int slaveId) {

        Map<Integer, SourcesBatch> batches = new HashMap<>();

        for (Source source : sources) {
            // Create group if it not exit //
            SourcesBatch sourcesBatch;
            if (batches.containsKey(source.getPollingTime())) {
                sourcesBatch = batches.get(source.getPollingTime());
            } else {
                sourcesBatch = new SourcesBatch();
                batches.put(source.getPollingTime(), sourcesBatch);
            }
            // Put source into Batch //
            sourcesBatch.addSource(source, slaveId);
        }
        return batches;
    }

    public void start(DataLoggerConfiguration configuration) {

        shutDown();

        modbusFactory = new ModbusFactory();

        logger.debug(this.getClass().getSimpleName() + " starting with following configuration : " + configuration);

        if (!configuration.getTcpSlaves().isEmpty()) {
            for (TcpSlave slave : configuration.getTcpSlaves()) {

                IpParameters ipParameters = new IpParameters();
                ipParameters.setHost(slave.getHost());
                ipParameters.setPort(slave.getPort());
                ModbusMaster master = modbusFactory.createTcpMaster(ipParameters, false);

                createAndRunPollers(master, slave.getSources(), slave.getId());
            }
        }

        if (!configuration.getSerialConfiguration().getSlaves().isEmpty()) {

            SerialConfiguration serialConfiguration = configuration.getSerialConfiguration();

            ModbusMaster master = modbusFactory.createRtuMaster(SerialPort.getWrapper(serialConfiguration));

            for (SerialSlave slave : serialConfiguration.getSlaves())
                createAndRunPollers(master, slave.getSources(), slave.getId());

        }
    }

    private void createAndRunPollers(ModbusMaster modbusMaster, List<Source> sources, int slaveId) {

        pollerExecutorService = Executors.newSingleThreadScheduledExecutor();

        masters.add(modbusMaster);
        try {
            modbusMaster.init();
        } catch (ModbusInitException e) {
            e.printStackTrace();
        }

        Map<Integer, SourcesBatch> batches = groupSourcesByInterval(sources, slaveId);

        // Start Poller task for each interval //
        for (Integer pollingInterval : batches.keySet()) {
            Poller poller = new Poller(modbusMaster, batches.get(pollingInterval));
            pollerExecutorService.scheduleAtFixedRate(poller, pollingInterval, pollingInterval, TimeUnit.SECONDS);
            logger.debug(poller.getClass().getSimpleName() + " #" + poller.hashCode()
                    + " created with interval " + pollingInterval + " seconds.");
        }
    }

    public void shutDown() {
        if (pollerExecutorService != null && !pollerExecutorService.isShutdown()) {
            logger.debug(this.getClass().getSimpleName() + " shutting down.");

            pollerExecutorService.shutdownNow();
            for (ModbusMaster master : masters)
                master.destroy();
            masters.clear();
            if (pollerExecutorService.isShutdown()) {
                logger.debug("Poller service is shutdown.");
            }
            pollerExecutorService = null;
        }
    }

    private void handleResults(List<Source> sources, BatchResults<Source> results) {
        if (entryDao == null)
            return;
        for (Source source : sources) {
            logger.debug(source.getName() + " | " + results.getValue(source) + " | " + source.getUnits());
            entryDao.add(new Entry(source.getName(), results.getValue(source).toString(), source.getUnits()));
        }

    }

    private class Poller implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(Poller.class);
        private ModbusMaster master;
        private SourcesBatch batch;

        Poller(ModbusMaster master, SourcesBatch batch) {
            this.master = master;
            this.batch = batch;
        }

        @Override
        public void run() {
            try {
                logger.debug("Batch #" + batch.hashCode() + " polling.");
                BatchResults<Source> results = master.send(batch);
                handleResults(batch.getSources(), results);
            } catch (ModbusTransportException | ErrorResponseException e) {
                logger.error("Modbus error with Batch #" + batch.hashCode() + " " + e.getMessage());
            }
        }
    }
}
