package datalogger.modbus.configuration;

import java.util.List;

/**
 * @author Konstantin Kosmachevskiy
 */
public abstract class Slave {
    abstract public int getId();

    abstract public List<Source> getSources();
}

