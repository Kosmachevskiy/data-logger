package datalogger.configuration;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class TcpSlave {
    public static final int DEFAULT_PORT = 502;
    public static final String DEFAULT_HOST = "127.0.0.1";

    @XmlAttribute
    private int id = 0;
    @XmlAttribute
    private String host = DEFAULT_HOST;
    @XmlAttribute
    private int port = DEFAULT_PORT;
    @XmlElement(name = "source")
    private List<Source> sources = new ArrayList<Source>();



}
