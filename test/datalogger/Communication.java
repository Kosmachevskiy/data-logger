package datalogger;

public class Communication {

//    public static void main(String args[]) throws ErrorResponseException, ModbusTransportException, ModbusInitException {
//
//        DataLoggerConfiguration configuration = DataLoggerConfiguration.createDemoConfig();
//        ModbusFactory modbusFactory = new ModbusFactory();
//
//        if (!configuration.getTcpSlaves().isEmpty()) {
//            for (TcpSlave slave : configuration.getTcpSlaves()) {
//                IpParameters ipParameters = new IpParameters();
//                ipParameters.setHost(slave.getHost());
//                ipParameters.setPort(slave.getPort());
//
//                ModbusMaster master = modbusFactory.createTcpMaster(ipParameters, false);
//                Map<Integer, SourcesBatch> batches = new HashMap<>();
//
//                putSourcesIntoBatches(batches, slave.getSources(), slave.getId());
//
//
//                // Read and print
//                for (SourcesBatch batch : batches.values()) {
//                    master.init();
//                    BatchResults<Source> results = master.send(batch);
//                    for (Source source: batch.getSources())
//                        System.out.println(results.getValue(source));
//
//                    master.destroy();
//                }
//            }
//        }

//        if (!configuration.getSerialConfiguration().getSlaves().isEmpty()) {
//
//            SerialConfiguration serialConfiguration = configuration.getSerialConfiguration();
//            SerialPort port = SerialPort.getCommPort("/dev/ttyUSB1");
//
//            port.setBaudRate(serialConfiguration.getBaudRate());
//            port.setNumDataBits(serialConfiguration.getDataBits());
//            port.setNumStopBits(serialConfiguration.getStopBits());
//            port.setParity(serialConfiguration.getParity());
//
//            ModbusMaster master = modbusFactory.createRtuMaster(new PortWrapper(port));
//
//
//            Map<Integer, SourcesBatch> batches = new HashMap<>();
//
//            for (SerialSlave slave : serialConfiguration.getSlaves())
//                putSourcesIntoBatches(batches, slave.getSources(), slave.getId());
//
//        }
//    }

    /**
     * Wrapper for <code>BatchRead</code> contains also <Code>List<Sources></Code>
     * to store <code>Sources</code> that was added into <code>BatchRead</code>
     */
//    private static class SourcesBatch extends BatchRead<Source> {
//        private List<Source> sources = new ArrayList<>();
//
//        public List<Source> getSources() {
//            return sources;
//        }
//
//        public void setSources(List<Source> sources) {
//            this.sources = sources;
//        }
//
//        /**
//         * Puts into read batch <code>BatchRead<Source></code>
//         *
//         * @param source
//         * @param slaveId
//         */
//        public void addSource(Source source, int slaveId) {
//            sources.add(source);
//            switch (source.getType()) {
//                case COIL:
//                    addLocator(source, BaseLocator.coilStatus(
//                            slaveId, source.getAddress()));
//                    break;
//                case INPUT:
//                    addLocator(source, BaseLocator.inputStatus(
//                            slaveId, source.getAddress()));
//                    break;
//                case INPUT_REGISTER:
//                    addLocator(source, BaseLocator.inputRegister(
//                            slaveId, source.getAddress(),
//                            mapDataTypeToNativeDataType(source.getDataType())));
//                    break;
//                case HOLDING_REGISTER:
//                    addLocator(source, BaseLocator.holdingRegister(
//                            slaveId, source.getAddress(),
//                            mapDataTypeToNativeDataType(source.getDataType())));
//                    break;
//            }
//        }
//
//        private static int mapDataTypeToNativeDataType(Source.DataType dataType) {
//            try {
//                Field field = DataType.class.getDeclaredField(dataType.name());
//                return field.getInt(dataType);
//            } catch (NoSuchFieldException e) {
//                e.printStackTrace(); //TODO: log this
//            } catch (IllegalAccessException e) {
//                e.printStackTrace(); //TODO: log this
//            }
//            return -1;
//        }
//    }

//    /**
//     * Puts sources into batches according to their polling interval
//     *
//     * @param batches
//     * @param sources
//     * @param slaveId
//     */
//    private static void putSourcesIntoBatches(Map<Integer, SourcesBatch> batches, List<Source> sources, int slaveId) {
//
//        for (Source source : sources) {
//            SourcesBatch sourcesBatch;
//            if (batches.containsKey(source.getPollingTime())) {
//                sourcesBatch = batches.get(source.getPollingTime());
//            } else {
//                sourcesBatch = new SourcesBatch();
//                batches.put(source.getPollingTime(), sourcesBatch);
//            }
//            sourcesBatch.addSource(source, slaveId);
//        }
//    }

}