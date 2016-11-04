package datalogger.dao;

import datalogger.AppConfig;
import datalogger.model.Entry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Konstantin Kosmachevskiy on 04.11.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class, loader = AnnotationConfigContextLoader.class)
public class EntryDaoJdbcTest {

    static final Entry[] TEST_DATA = {
            new Entry("12.1", "units"),
            new Entry("14.2", "units"),
            new Entry("13.3", "units"),
            new Entry("11.4", "units"),
    };

    @Autowired
    private EntryDao entryDao;

    @Before
    public void before() {
        try {
            Connection connection = getConnection();

            connection.createStatement().execute(EntrySql.DELETE_ALL);
            for (Entry entry : TEST_DATA) {
                connection.createStatement().execute(
                        String.format(EntrySql.ADD, entry.getDate(), entry.getTime(), entry.getValue(), entry.getUnit()));
            }

            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void after() {
    }

    private long countAll() {
        long count = -1;
        try {
            Connection connection = getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery(EntrySql.COUNT_ALL);
            resultSet.next();
            count = resultSet.getLong(1);
            connection.close();
        } finally {
            return count;
        }

    }

    @Test
    public void youCanAdd() throws Exception {
        long before = countAll();
        entryDao.add(new Entry("666.66", "unit"));
        long after = countAll();

        assertEquals(++before, after);
    }

    Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(AppConfig.DRIVER_NAME);
        return DriverManager.getConnection(AppConfig.DATA_BASE);
    }

    @Test
    public void youCanDeleteById() throws Exception {
        ResultSet resultSet = getConnection().createStatement().executeQuery(EntrySql.GET_ALL);
        List<Entry> entries = new ArrayList<>();
        while (resultSet.next())
            entries.add(new Entry(resultSet.getLong(1), resultSet.getDate(2), resultSet.getTime(3),
                    resultSet.getString(4), resultSet.getString(5)));


        Entry entry = entries.get(0);
        entryDao.deleteById(entry.getId());

        assertEquals(TEST_DATA.length-1, entryDao.countEntries());
        assertFalse(entryDao.getAll().contains(entry));
    }

    @Test
    public void youCanCountEntries() {
        assertEquals(TEST_DATA.length, entryDao.countEntries());
    }

    @Test
    public void youCanGetAllEntries() throws Exception {
        assertEquals(TEST_DATA.length, entryDao.getAll().size());
    }

    @Test
    public void youCanDeleteAll() {
        entryDao.deleteAll();
        assertEquals(0, countAll());
    }

}