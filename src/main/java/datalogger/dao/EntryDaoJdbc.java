package datalogger.dao;

import datalogger.model.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Created by Konstantin Kosmachevskiy on 04.11.16.
 */
public class EntryDaoJdbc implements EntryDao {
    private static final EntryMapper ENTRY_MAPPER = new EntryMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public void add(Entry entry) {
        jdbcTemplate.execute(String.format(
                EntrySqlConstants.ADD, entry.getDate(), entry.getTime(), entry.getValue(), entry.getUnit()));
    }

    @Override
    public void deleteById(long id) {
        jdbcTemplate.execute(String.format(EntrySqlConstants.DELETE_BY_ID, id));
    }

    @Override
    public List<Entry> getAll() {
        return jdbcTemplate.query(EntrySqlConstants.GET_ALL, ENTRY_MAPPER);
    }

    @Override
    public long countEntries() {
        return jdbcTemplate.queryForObject(EntrySqlConstants.COUNT_ALL, Long.class);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute(EntrySqlConstants.DELETE_ALL);
    }

    /**
     * Created by Konstantin Kosmachevskiy on 04.11.16.
     */
    private static final class EntrySqlConstants {
/*        static final String CREATE_SCHEMA = // TODO: delete this variable?
                "CREATE TABLE IF NOT EXISTS entries (id identity, date DATE, time TIME, value VARCHAR(255), unit VARCHAR(20));";*/
        static final String ADD = "INSERT INTO entries (date, time , value, unit) VALUES('%s', '%s', '%s','%s');";
        static final String GET_ALL = "SELECT * FROM ENTRIES";
        static final String COUNT_ALL = "SELECT COUNT(*) FROM ENTRIES";
        static final String DELETE_BY_ID = "DELETE FROM entries WHERE id=%s";
        static final String DELETE_ALL = "DELETE FROM entries;";
    }
}
