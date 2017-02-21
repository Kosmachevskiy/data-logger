package datalogger.modbus.configuration;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class DataLoggerConfiguration {

    @XmlElement(name = "serial")
    private SerialConfiguration serialConfiguration = new SerialConfiguration();
    @XmlElementWrapper(name = "tcp")
    @XmlElement(name = "slave")
    private List<TcpSlave> tcpSlaves = new ArrayList<TcpSlave>();

    public static DataLoggerConfiguration createDemoConfig() {
        DataLoggerConfiguration configuration = new DataLoggerConfiguration();

        TcpSlave tcpSlave = new TcpSlave();
        tcpSlave.setId(5);
        tcpSlave.getSources().add(new Source("(TCP) Door contact", "Open/Close", Source.Type.COIL, 100, 3));
        tcpSlave.getSources().add(new Source("(TCP) Binary Sensor", "True/False", Source.Type.INPUT, 200, 3));
        tcpSlave.getSources().add(new Source("(TCP) Brightness", "Lux", Source.Type.INPUT_REGISTER,
                300, 5, Source.DataType.FOUR_BYTE_INT_UNSIGNED));
        tcpSlave.getSources().add(new Source("(TCP) Weight", "Kg", Source.Type.HOLDING_REGISTER,
                400, 9, Source.DataType.FOUR_BYTE_FLOAT));

        SerialSlave serialSlave = new SerialSlave();
        serialSlave.setId(1);
        serialSlave.getSources().add(new Source("(Serial) Door contact", "Open/Close", Source.Type.COIL, 100, 3));
        serialSlave.getSources().add(new Source("(Serial) Binary Sensor", "True/False", Source.Type.INPUT, 200, 3));
        serialSlave.getSources().add(new Source("(Serial) Brightness", "Lux", Source.Type.INPUT_REGISTER,
                300, 5, Source.DataType.FOUR_BYTE_INT_UNSIGNED));
        serialSlave.getSources().add(new Source("(Serial) Weight", "Kg", Source.Type.HOLDING_REGISTER,
                400, 9, Source.DataType.FOUR_BYTE_FLOAT));


        configuration.getTcpSlaves().add(tcpSlave);
        configuration.getSerialConfiguration().getSlaves().add(serialSlave);

        return configuration;
    }
}
