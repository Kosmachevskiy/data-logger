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
}
