package datalogger.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.sql.Date;
import java.sql.Time;

@Data
@AllArgsConstructor
public class Entry {
    private long id;
    private Date date;
    private Time time;
    private String value;
    private String unit;

    public Entry(String value, String unit) {
        this.date = new Date(System.currentTimeMillis());
        this.time = new Time(System.currentTimeMillis());
        this.value = value;
        this.unit = unit;
    }
}
