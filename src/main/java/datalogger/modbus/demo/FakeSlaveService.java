package datalogger.modbus.demo;

import com.serotonin.modbus4j.BasicProcessImage;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusSlaveSet;
import com.serotonin.modbus4j.code.RegisterRange;
import com.serotonin.modbus4j.exception.ModbusInitException;
import datalogger.modbus.SerialPort;
import datalogger.modbus.configuration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service provide TCP and Serial Modbus slaves according to config to emulate real devices.
 *
 * @author Konstantin Kosmachevskiy
 */
public class FakeSlaveService {
    private static final Logger logger = LoggerFactory.getLogger(FakeSlaveService.class);

    private DataLoggerConfiguration configuration;
    private ModbusFactory factory = new ModbusFactory();

    private ExecutorService tcpSlaves;
    private ExecutorService tcpUpdaters;
    private List<ModbusSlaveSet> tcpModbusSlaveSets;

    private ExecutorService serialSlaves;
    private ExecutorService serialUpdaters;
    private ModbusSlaveSet serialModbusSlaveSet;

    public FakeSlaveService(DataLoggerConfiguration configuration) {
        this.configuration = configuration;
    }

    private static BasicProcessImage buildProcessImage(int slaveId) {
        BasicProcessImage processImage = new BasicProcessImage(slaveId);
        processImage.setAllowInvalidAddress(true);
        processImage.setInvalidAddressValue(Short.MIN_VALUE);
        processImage.setExceptionStatus((byte) 151);

        return processImage;
    }

    public DataLoggerConfiguration getConfiguration() {
        return configuration;
    }

    public void startTcpSlaves() {
        if (configuration == null) return;
        if (configuration.getTcpSlaves() == null || configuration.getTcpSlaves().size() == 0) return;

        tcpSlaves = Executors.newFixedThreadPool(configuration.getTcpSlaves().size());
        tcpUpdaters = Executors.newFixedThreadPool(configuration.getTcpSlaves().size());
        tcpModbusSlaveSets = new ArrayList<>();

        for (TcpSlave slave : configuration.getTcpSlaves()) {
            ModbusSlaveSet modbusSlaveSet = factory.createTcpSlave(slave.getPort(), false);
            BasicProcessImage image = buildProcessImage(slave.getId());
            modbusSlaveSet.addProcessImage(image);
            tcpSlaves.submit(new SlaveRunner(modbusSlaveSet));
            tcpUpdaters.submit(new SlaveUpdater(modbusSlaveSet, slave));
            tcpModbusSlaveSets.add(modbusSlaveSet);
        }
    }

    public void stopTcpSlaves() {
        if (tcpSlaves != null && tcpUpdaters != null) {

            logger.debug("TCP Slaves stopping.");


            tcpSlaves.shutdownNow();
            tcpSlaves = null;
            tcpUpdaters.shutdownNow();
            tcpUpdaters = null;

            for (ModbusSlaveSet set : tcpModbusSlaveSets)
                set.stop();

            tcpModbusSlaveSets.clear();
        }
    }

    public void startSerialSlaves(String serialPort) {
        if (configuration == null)
            return;
        if (configuration.getSerialConfiguration() == null)
            return;
        if (configuration.getSerialConfiguration().getSlaves() == null ||
                configuration.getSerialConfiguration().getSlaves().size() == 0)
            return;
        if (serialPort == null || serialPort.length() == 0)
            return;

        //-- Creating local config to run slave port --//
        SerialConfiguration serialConfiguration = new SerialConfiguration();
        serialConfiguration.setPort(serialPort); //!!!
        serialConfiguration.setBaudRate(configuration.getSerialConfiguration().getBaudRate());
        serialConfiguration.setDataBits(configuration.getSerialConfiguration().getDataBits());
        serialConfiguration.setParity(configuration.getSerialConfiguration().getParity());
        serialConfiguration.setStopBits(configuration.getSerialConfiguration().getStopBits());


        serialModbusSlaveSet = factory.createRtuSlave(SerialPort.getWrapper(serialConfiguration));
        serialSlaves = Executors.newSingleThreadExecutor();
        serialUpdaters = Executors.newFixedThreadPool(configuration.getSerialConfiguration().getSlaves().size());

        serialSlaves.submit(new SlaveRunner(serialModbusSlaveSet));

        for (SerialSlave slave : configuration.getSerialConfiguration().getSlaves()) {
            serialModbusSlaveSet.addProcessImage(buildProcessImage(slave.getId()));
            serialUpdaters.submit(new SlaveUpdater(serialModbusSlaveSet, slave));
        }
    }

    public void stopSerialSlaves() {
        if (serialSlaves != null && serialUpdaters != null) {
            logger.debug("Serial Slaves stopping.");

            serialSlaves.shutdownNow();
            serialSlaves = null;
            serialUpdaters.shutdownNow();
            serialUpdaters = null;

            serialModbusSlaveSet.stop();
            serialModbusSlaveSet = null;
        }
    }

    private class SlaveUpdater implements Runnable {
        private static final int RANGE = 100;
        private final Logger logger = LoggerFactory.getLogger(SlaveUpdater.class);
        private ModbusSlaveSet slaveSet;
        private Slave slave;
        private Random random = new Random(System.currentTimeMillis());

        public SlaveUpdater(ModbusSlaveSet slaveSet, Slave slave) {
            this.slaveSet = slaveSet;
            this.slave = slave;
        }

        @Override
        public void run() {
            BasicProcessImage image = (BasicProcessImage) slaveSet.getProcessImage(slave.getId());
            List<Source> sources = slave.getSources();

            long sleepTime = defineDelay();

            try {
                logger.debug(this.hashCode() + ". " + slave.toString() + " updater starting");

                while (true) {

                    logger.debug(this.hashCode() + ". " + slave.toString() + " updater updating");

                    for (Source source : sources) {
                        switch (source.getType()) {
                            case INPUT:
                                image.setInput(source.getAddress(), random.nextBoolean());
                                break;
                            case COIL:
                                image.setCoil(source.getAddress(), random.nextBoolean());
                                break;
                            case INPUT_REGISTER:
                                image.setNumeric(RegisterRange.INPUT_REGISTER, source.getAddress(),
                                        Source.mapDataTypeToNativeDataType(source.getDataType()), random.nextInt(RANGE));
                                break;
                            case HOLDING_REGISTER:
                                image.setNumeric(RegisterRange.HOLDING_REGISTER, source.getAddress(),
                                        Source.mapDataTypeToNativeDataType(source.getDataType()), random.nextInt(RANGE));
                                break;
                        }
                    }

                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                logger.debug(this.hashCode() + ". " + slave.toString() + " updater stopping");
            }
        }


        private long defineDelay() {
            long delay = slave.getSources().get(0).getPollingTime();

            for (Source source : slave.getSources())
                if (source.getPollingTime() < delay)
                    delay = source.getPollingTime();

            return delay * 1000;
        }
    }

    private class SlaveRunner implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(SlaveRunner.class);
        private ModbusSlaveSet slaveSet;

        public SlaveRunner(ModbusSlaveSet slaveSet) {
            this.slaveSet = slaveSet;
        }

        @Override
        public void run() {
            try {
                logger.debug(this.getClass().getSimpleName() + " starting.");
                slaveSet.start();
            } catch (ModbusInitException e) {
                logger.debug(this.getClass().getSimpleName() + " stop. " + e.getCause());
            }
        }
    }
}
