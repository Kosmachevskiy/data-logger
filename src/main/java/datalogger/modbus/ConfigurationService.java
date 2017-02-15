package datalogger.modbus;

import datalogger.modbus.configuration.DataLoggerConfiguration;
import datalogger.modbus.configuration.SerialSlave;
import datalogger.modbus.configuration.Source;
import datalogger.modbus.configuration.TcpSlave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Konstantin Kosmachevskiy
 */
public class ConfigurationService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    private final static String FILE_NAME = "data-logger-config.xml";
    private final String configFileFullPath;

    public ConfigurationService(String path) {
        this.configFileFullPath = path + FILE_NAME;
        File file = new File(configFileFullPath);
        if (!file.exists()) {
            save(new DataLoggerConfiguration());
            logger.debug("Config file had created. Path: " + configFileFullPath);
        }
    }

    public DataLoggerConfiguration load() {
        DataLoggerConfiguration configuration = new DataLoggerConfiguration();
        File file = new File(configFileFullPath);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DataLoggerConfiguration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            configuration = (DataLoggerConfiguration) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return configuration;
    }

    public boolean save(DataLoggerConfiguration configuration) {
        File file = new File(configFileFullPath);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DataLoggerConfiguration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(configuration, file);
            return true;
        } catch (JAXBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save(InputStream inputStream) {
        try {
            File file = new File(configFileFullPath);
            file.delete();
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            while (inputStream.available() > 0)
                fileOutputStream.write(inputStream.read());
            fileOutputStream.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public DataLoggerConfiguration createDemoConfig() {
        DataLoggerConfiguration configuration = new DataLoggerConfiguration();

        TcpSlave tcpSlave = new TcpSlave();
        tcpSlave.setId(1);
        tcpSlave.getSources().add(new Source("Door contact", "Open/Close", Source.Type.COIL, 100, 3));
        tcpSlave.getSources().add(new Source("Binary Sensor", "True/False", Source.Type.INPUT, 200, 3));
        tcpSlave.getSources().add(new Source("Brightness", "Lux", Source.Type.INPUT_REGISTER,
                300, 5, Source.DataType.FOUR_BYTE_INT_UNSIGNED));
        tcpSlave.getSources().add(new Source("Weight", "Kg", Source.Type.HOLDING_REGISTER,
                400, 9, Source.DataType.FOUR_BYTE_FLOAT));

        SerialSlave serialSlave = new SerialSlave();
        serialSlave.setId(1);
        serialSlave.setSources(tcpSlave.getSources());

        configuration.getTcpSlaves().add(tcpSlave);
        configuration.getSerialConfiguration().getSlaves().add(serialSlave);

        return configuration;
    }

    public File getConfigFileFullPath() {
        return new File(configFileFullPath);
    }

}
