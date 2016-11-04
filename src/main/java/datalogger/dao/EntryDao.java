package datalogger.dao;

import datalogger.model.Entry;

import java.sql.Connection;
import java.util.List;

/**
 * Created by Konstantin Kosmachevskiy on 04.11.16.
 */
public interface EntryDao {
    void add(Entry entry);
    void deleteById(long id);
    List<Entry> getAll();
    long countEntries();
    void deleteAll();
}
