package datalogger.modbus;

import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.locator.BaseLocator;
import datalogger.configuration.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Kosmachevskiy
 */
class SourcesBatch extends BatchRead<Source> {

    private static final Logger logger = LoggerFactory.getLogger(SourcesBatch.class);

    private List<Source> sources = new ArrayList<>();

    static int mapDataTypeToNativeDataType(Source.DataType dataType) throws IllegalArgumentException {
        if (dataType == null)
            throw new IllegalArgumentException();
        try {
            Field field = DataType.class.getDeclaredField(dataType.name());
            return field.getInt(dataType);
        } catch (Exception e) {
            logger.error("Type mapping failure. " + e.getMessage());
            return -1;
        }
    }

    public List<Source> getSources() {
        return sources;
    }

    /**
     * Puts into read batch <code>BatchRead<Source></code>
     *
     * @param source
     * @param slaveId
     */
    public void addSource(Source source, int slaveId) {
        switch (source.getType()) {
            case COIL:
                addLocator(source, BaseLocator.coilStatus(
                        slaveId, source.getAddress()));
                break;
            case INPUT:
                addLocator(source, BaseLocator.inputStatus(
                        slaveId, source.getAddress()));
                break;
            case INPUT_REGISTER:
                try {
                    addLocator(source, BaseLocator.inputRegister(
                            slaveId, source.getAddress(),
                            mapDataTypeToNativeDataType(source.getDataType())));
                } catch (IllegalArgumentException e) {
                    logger.error(Source.DataType.class.getSimpleName() + " is not specified for " +
                            Source.class.getSimpleName() + " with name \"" + source.getName() + "\"");
                    return;
                }
                break;
            case HOLDING_REGISTER:
                try {
                    addLocator(source, BaseLocator.holdingRegister(
                            slaveId, source.getAddress(),
                            mapDataTypeToNativeDataType(source.getDataType())));
                } catch (IllegalArgumentException e) {
                    logger.error(Source.DataType.class.getSimpleName() + " is not specified for " +
                            Source.class.getSimpleName() + " with name \"" + source.getName() + "\"");
                    return;
                }
                break;
            default:
                return;
        }
        sources.add(source);
    }

}
