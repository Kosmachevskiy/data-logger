package datalogger.modbus.configuration;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class SerialConfiguration {
    private final static int DEFAULT_BAUND_RATE = 9600;
    private final static int DEFAULT_DATA_BITS = 8;
    private final static int DEFAULT_STOP_BITS = 1;
    private final static int DEFAULT_PARITY = 0;
    private final static String DEFAULT_PORT = "/dev/ttyUSB0";

    @XmlAttribute
    private int baudRate = DEFAULT_BAUND_RATE;
    @XmlAttribute
    private int dataBits = DEFAULT_DATA_BITS;
    @XmlAttribute
    private int stopBits = DEFAULT_STOP_BITS;
    @XmlAttribute
    private int parity = DEFAULT_PARITY;
    @XmlTransient
    private String port = DEFAULT_PORT;
    @XmlElement(name = "slave")
    private List<SerialSlave> slaves = new ArrayList<SerialSlave>();
}
