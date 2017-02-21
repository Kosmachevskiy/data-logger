package datalogger.model.dao;

import datalogger.model.Entry;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EntryDaoJdbc extends JdbcDaoSupport implements EntryDao {
    private static final EntryMapper ENTRY_MAPPER = new EntryMapper();

    @Override
    public void add(Entry entry) {
        getJdbcTemplate().execute(String.format(
                EntrySqlConstants.ADD, entry.getDate(), entry.getTime(), entry.getValue(), entry.getUnit(), entry.getName()));
    }

    @Override
    public void deleteById(long id) {
        getJdbcTemplate().execute(String.format(EntrySqlConstants.DELETE_BY_ID, id));
    }

    @Override
    public List<Entry> getAll() {
        return getJdbcTemplate().query(EntrySqlConstants.GET_ALL, ENTRY_MAPPER);
    }

    @Override
    public long countEntries() {
        return getJdbcTemplate().queryForObject(EntrySqlConstants.COUNT_ALL, Long.class);
    }

    @Override
    public void deleteAll() {
        getJdbcTemplate().execute(EntrySqlConstants.DELETE_ALL);
    }

    private static final class EntrySqlConstants {
        static final String ADD = "INSERT INTO entries (date, time , value, unit, name) " +
                "VALUES('%s', '%s', '%s','%s', '%s');";
        static final String GET_ALL = "SELECT * FROM ENTRIES";
        static final String COUNT_ALL = "SELECT COUNT(*) FROM ENTRIES";
        static final String DELETE_BY_ID = "DELETE FROM entries WHERE id=%s";
        static final String DELETE_ALL = "TRUNCATE TABLE entries;";
    }

    private static class EntryMapper implements RowMapper<Entry> {

        @Override
        public Entry mapRow(ResultSet resultSet, int i) throws SQLException {
            Entry entry = new Entry(
                    resultSet.getLong("id"),
                    resultSet.getDate("date"),
                    resultSet.getTime("time"),
                    resultSet.getString("value"),
                    resultSet.getString("unit"),
                    resultSet.getString("name"));
            return entry;
        }
    }
}
