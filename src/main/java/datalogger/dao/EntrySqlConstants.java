package datalogger.dao;

/**
 * Created by Konstantin Kosmachevskiy on 04.11.16.
 */
public final class EntrySqlConstants {
    public static final String CREATE_SCHEMA =
            "CREATE TABLE IF NOT EXISTS entries (id identity, date DATE, time TIME, value VARCHAR(255), unit VARCHAR(20));";
    static final String ADD = "INSERT INTO entries (date, time , value, unit) VALUES('%s', '%s', '%s','%s');";
    static final String GET_ALL = "SELECT * FROM ENTRIES";
    static final String COUNT_ALL = "SELECT COUNT(*) FROM ENTRIES";
    static final String DELETE_BY_ID = "DELETE FROM entries WHERE id=%s";
    static final String DELETE_ALL = "DELETE FROM entries;";
}
