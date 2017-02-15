package datalogger.model.dao;

import datalogger.AppConfig;
import datalogger.model.Entry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class}, loader = AnnotationConfigContextLoader.class)
@PropertySources({
        @PropertySource("classpath:app.properties"),
        @PropertySource("classpath:db.properties")
})
public class EntryDaoJdbcTest {

    private final Entry[] TEST_DATA = {
            new Entry("Source1", "12.1", "units"),
            new Entry("Source2", "14.2", "units"),
            new Entry("Source3", "13.3", "units"),
            new Entry("Source4", "11.4", "units"),
    };

    @Autowired
    private EntryDao entryDao;
    @Autowired
    private Environment env;


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
        Class.forName(env.getProperty("db.driver"));
        return DriverManager.getConnection(env.getProperty("db.url"));
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