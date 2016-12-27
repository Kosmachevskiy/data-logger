package datalogger.modbus;

import com.serotonin.modbus4j.code.DataType;
import datalogger.configuration.Source;
import org.junit.*;

import java.util.List;

/**
 * @author Konstantin Kosmachevskiy
 */
public class SourcesBatchTest {

    private static SourcesBatch sourcesBatch;

    @BeforeClass
    public static void setUpClass() {
        sourcesBatch = new SourcesBatch();
    }

    @Test
    public void mapDataTypeToNativeDataType() throws Exception {
        int expectedValue = DataType.EIGHT_BYTE_FLOAT;
        int actualValue = SourcesBatch.mapDataTypeToNativeDataType(Source.DataType.EIGHT_BYTE_FLOAT);

        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public  void mapDataTypeToNativeDataTypeException(){
        // By default DataType is not specified
        Source source = new Source();
        sourcesBatch.mapDataTypeToNativeDataType(source.getDataType());
    }

    @Test
    public void youCanAddAndGetSources() throws Exception {

        sourcesBatch.addSource(new Source("SomeName1", "SomeUnits",
                Source.Type.INPUT_REGISTER, 100, 1000, Source.DataType.FOUR_BYTE_FLOAT), 1);
        sourcesBatch.addSource(new Source("SomeName2", "SomeUnits",
                Source.Type.HOLDING_REGISTER, 200, 1000, Source.DataType.FOUR_BYTE_FLOAT), 1);
        sourcesBatch.addSource(new Source("SomeName3", "SomeUnits",
                Source.Type.INPUT, 300, 1000), 1);
        sourcesBatch.addSource(new Source("SomeName4", "SomeUnits",
                Source.Type.COIL, 400, 1000), 1);

        List<Source> sources = sourcesBatch.getSources();
        Assert.assertEquals(4, sources.size());
    }

}