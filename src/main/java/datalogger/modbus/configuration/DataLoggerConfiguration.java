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

}
