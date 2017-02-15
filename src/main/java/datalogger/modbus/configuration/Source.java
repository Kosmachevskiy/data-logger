package datalogger.modbus.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Source {

    @XmlAttribute
    private String name = "";
    @XmlAttribute
    private  String units ="";
    @XmlAttribute
    private Type type;
    @XmlAttribute
    private int address;
    @XmlAttribute
    private int pollingTime;
    @XmlAttribute
    private DataType dataType;

    public Source (String name, String units, Type type, int address, int pollingTime){
        this.name = name;
        this.type = type;
        this.address = address;
        this.units = units;
        this.pollingTime = pollingTime;
    }

    public enum Type {
        INPUT,
        COIL,
        INPUT_REGISTER,
        HOLDING_REGISTER
    }

    //TODO: Rename DataType values to something like FOUR_BYTE_FLOAT_BIG_ENDIAN instead of FOUR_BYTE_FLOAT_SWAPPED
    public enum DataType{
        BINARY,
        TWO_BYTE_INT_UNSIGNED,
        TWO_BYTE_INT_SIGNED,
        TWO_BYTE_INT_UNSIGNED_SWAPPED,
        TWO_BYTE_INT_SIGNED_SWAPPED,
        FOUR_BYTE_INT_UNSIGNED,
        FOUR_BYTE_INT_SIGNED,
        FOUR_BYTE_INT_UNSIGNED_SWAPPED,
        FOUR_BYTE_INT_SIGNED_SWAPPED,
        FOUR_BYTE_INT_UNSIGNED_SWAPPED_SWAPPED,
        FOUR_BYTE_INT_SIGNED_SWAPPED_SWAPPED,
        FOUR_BYTE_FLOAT,
        FOUR_BYTE_FLOAT_SWAPPED,
        FOUR_BYTE_FLOAT_SWAPPED_INVERTED,
        EIGHT_BYTE_INT_UNSIGNED,
        EIGHT_BYTE_INT_SIGNED,
        EIGHT_BYTE_INT_UNSIGNED_SWAPPED,
        EIGHT_BYTE_INT_SIGNED_SWAPPED,
        EIGHT_BYTE_FLOAT,
        EIGHT_BYTE_FLOAT_SWAPPED,
        TWO_BYTE_BCD,
        FOUR_BYTE_BCD,
        FOUR_BYTE_BCD_SWAPPED,
        CHAR,
        VARCHAR
    }

}
