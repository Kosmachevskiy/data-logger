package datalogger.modbus;

import com.fazecast.jSerialComm.SerialPort;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import datalogger.configuration.*;
import datalogger.dao.EntryDao;
import datalogger.model.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Konstantin Kosmachevskiy
 */
public class ModbusService {

    private static final Logger logger = LoggerFactory.getLogger(ModbusService.class);
    private ModbusFactory modbusFactory;
    private ScheduledExecutorService service;
    @Autowired
    private EntryDao entryDao;

    {   // TODO: is it work as I expect?
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutDown();
            }
        });
    }

    public void start() {

        shutDown();

        service = Executors.newSingleThreadScheduledExecutor();
        modbusFactory = new ModbusFactory();

        DataLoggerConfiguration configuration = DataLoggerConfiguration.load();

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

            SerialPort port = SerialPort.getCommPort(serialConfiguration.getPort());

            port.setBaudRate(serialConfiguration.getBaudRate());
            port.setNumDataBits(serialConfiguration.getDataBits());
            port.setNumStopBits(serialConfiguration.getStopBits());
            port.setParity(serialConfiguration.getParity());

            ModbusMaster master = modbusFactory.createRtuMaster(new PortWrapper(port));

            for (SerialSlave slave : serialConfiguration.getSlaves())
                createAndRunPollers(master, slave.getSources(), slave.getId());

        }
    }

    private void createAndRunPollers(ModbusMaster modbusMaster, List<Source> sources, int slaveId) {

        Map<Integer, SourcesBatch> batches = groupSourcesByInterval(sources, slaveId);

        // Start Poller task for each interval //
        for (Integer pollingInterval : batches.keySet()) {
            Poller poller = new Poller(modbusMaster, batches.get(pollingInterval));
            service.scheduleAtFixedRate(poller, pollingInterval, pollingInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Groups Sources by polling interval
     */
    Map<Integer, SourcesBatch> groupSourcesByInterval(List<Source> sources, int slaveId) {

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

    public void shutDown() {
        if (service != null && !service.isShutdown()) {
            logger.debug(this.getClass().getSimpleName() + " shutting down.");
            service.shutdown();
        }
    }

    private void handleResults(List<Source> sources, BatchResults<Source> results) {
        for (Source source : sources) {
            logger.debug(source.getName() + " | " + results.getValue(source) + " | " + source.getUnits());
            entryDao.add(new Entry(source.getName(), results.getValue(source).toString(), source.getUnits()));
        }
    }

    private class Poller implements Runnable {
        private ModbusMaster master;
        private SourcesBatch batch;

        Poller(ModbusMaster master, SourcesBatch batch) {
            this.master = master;
            this.batch = batch;

        }

        @Override
        public void run() {
            try {
                master.init();
                BatchResults<Source> results = master.send(batch);
                handleResults(batch.getSources(), results);
                master.destroy();
            } catch (ModbusTransportException | ErrorResponseException | ModbusInitException e) {
                logger.error("Modbus error. " + e.getMessage());
            }
        }

    }

}
