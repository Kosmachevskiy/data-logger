package datalogger.dao;

import datalogger.model.Entry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Konstantin Kosmachevskiy on 04.11.16.
 */
public class EntryMapper implements RowMapper<Entry> {

    @Override
    public Entry mapRow(ResultSet resultSet, int i) throws SQLException {
        Entry entry = new Entry(
                resultSet.getLong("id"),
                resultSet.getDate("date"),
                resultSet.getTime("time"),
                resultSet.getString("value"),
                resultSet.getString("unit"));
        return entry;
    }
}
