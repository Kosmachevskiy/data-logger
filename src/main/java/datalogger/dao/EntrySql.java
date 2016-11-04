package datalogger.dao;

/**
 * Created by Konstantin Kosmachevskiy on 04.11.16.
 */
public final class EntrySql  {
    public static final String CREATE_SCHEMA =
            "CREATE TABLE IF NOT EXISTS entries (id identity, date DATE, time TIME, value VARCHAR(255), unit VARCHAR(20));";
    public static final String ADD = "INSERT INTO entries (date, time , value, unit) VALUES('%s', '%s', '%s','%s');";
    public static final String GET_ALL = "SELECT * FROM ENTRIES";
    public static final String COUNT_ALL = "SELECT COUNT(*) FROM ENTRIES";
    public static final String DELETE_BY_ID = "DELETE FROM entries WHERE id=%s";
    public static final String DELETE_ALL = "DELETE FROM entries;";
}
