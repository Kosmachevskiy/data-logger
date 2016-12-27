package datalogger.configuration;

import lombok.Data;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class DataLoggerConfiguration {
    //TODO: to change default location
    public static final String DEFAULT_CONFIG_FILE_LOCATION = "./data-logger-config.xml";

    @XmlElement(name = "serial")
    private SerialConfiguration serialConfiguration = new SerialConfiguration();
    @XmlElementWrapper(name = "tcp")
    @XmlElement(name = "slave")
    private List<TcpSlave> tcpSlaves = new ArrayList<TcpSlave>();

    static {
        File file = new File(DEFAULT_CONFIG_FILE_LOCATION);
        if (!file.exists())
            save(new DataLoggerConfiguration());
    }

    public static DataLoggerConfiguration load() {
        DataLoggerConfiguration configuration = new DataLoggerConfiguration();
        File file = new File(DEFAULT_CONFIG_FILE_LOCATION);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DataLoggerConfiguration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            configuration = (DataLoggerConfiguration) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return configuration;
    }

    public static boolean save(DataLoggerConfiguration configuration) {
        File file = new File(DEFAULT_CONFIG_FILE_LOCATION);
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

    public static DataLoggerConfiguration createDemoConfig() {
        DataLoggerConfiguration configuration = new DataLoggerConfiguration();

        TcpSlave tcpSlave = new TcpSlave();
        tcpSlave.setId(1);
        tcpSlave.getSources().add(new Source("Door contact", "Open/Close", Source.Type.COIL, 100, 5));
        tcpSlave.getSources().add(new Source("Binary Sensor", "True/False", Source.Type.INPUT, 200, 10));
        tcpSlave.getSources().add(new Source("Brightness", "Lux", Source.Type.INPUT_REGISTER,
                300, 10, Source.DataType.FOUR_BYTE_INT_UNSIGNED));
        tcpSlave.getSources().add(new Source("Weight", "Kg", Source.Type.HOLDING_REGISTER,
                400, 50, Source.DataType.FOUR_BYTE_FLOAT));

        SerialSlave serialSlave = new SerialSlave();
        serialSlave.setId(1);
        serialSlave.setSources(tcpSlave.getSources());

        configuration.getTcpSlaves().add(tcpSlave);
        configuration.getSerialConfiguration().getSlaves().add(serialSlave);

        return configuration;
    }

}
