package datalogger.configuration;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class SerialSlave {

    @XmlAttribute
    private int id = 0;
    @XmlElement(name = "source")
    private List<Source> sources = new ArrayList<Source>();

}