package datalogger.modbus;

import datalogger.modbus.configuration.DataLoggerConfiguration;
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

    public File getConfigFileFullPath() {
        return new File(configFileFullPath);
    }

}
