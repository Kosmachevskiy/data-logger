package datalogger.model.dao;

import datalogger.AppConfig;
import datalogger.model.Entry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Konstantin Kosmachevskiy
 */
public class EntryDaoJdbcTest {

    private static final String DRIVER = "org.h2.Driver";
    private static final String URL = "jdbc:h2:./data-logger-database-test";
    private static final Entry[] TEST_DATA = {
            new Entry("Source1", "12.1", "units"),
            new Entry("Source2", "14.2", "units"),
            new Entry("Source3", "13.3", "units"),
            new Entry("Source4", "11.4", "units"),
    };
    private static EntryDaoJdbc entryDao;

    @BeforeClass
    public static void beforeClass() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER);
        dataSource.setUrl(URL);
        entryDao = new EntryDaoJdbc();
        entryDao.setDataSource(dataSource);
        entryDao.getJdbcTemplate().execute(AppConfig.DB_SCHEMA);
    }

    @Before
    public void before() {
        try {
            Connection connection = getConnection();

            connection.createStatement().execute("DELETE FROM entries;");

            for (Entry entry : TEST_DATA) {
                connection.createStatement().execute(
                        String.format("INSERT INTO entries (date, time , name, value, unit) VALUES('%s', '%s', '%s','%s', '%s');",
                                entry.getDate(), entry.getTime(), entry.getName(), entry.getValue(), entry.getUnit()));
            }

            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long countAll() {
        long count = -1;
        try {
            Connection connection = getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT COUNT(*) FROM ENTRIES");
            resultSet.next();
            count = resultSet.getLong(1);
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;

    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER);
        return DriverManager.getConnection(URL);
    }

    @Test
    public void youCanAdd() throws Exception {
        long before = countAll();
        entryDao.add(new Entry("SomeName", "666.66", "unit"));
        long after = countAll();

        assertEquals(++before, after);
    }

    @Test
    public void youCanDeleteById() throws Exception {
        ResultSet resultSet = getConnection().createStatement().executeQuery("SELECT * FROM ENTRIES");
        List<Entry> entries = new ArrayList<>();
        while (resultSet.next())
            entries.add(new Entry(resultSet.getLong(1), resultSet.getDate(2), resultSet.getTime(3),
                    resultSet.getString(4), resultSet.getString(5), resultSet.getString(6)));


        Entry entry = entries.get(0);
        entryDao.deleteById(entry.getId());

        assertEquals(TEST_DATA.length - 1, countAll());
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