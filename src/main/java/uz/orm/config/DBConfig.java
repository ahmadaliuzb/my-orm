package uz.orm.config;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConfig {
    private static final String PROPERTIES_FILE = "my.properties";
    private static final String url;
    private static final String user;
    private static final String password;

    static {
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            Properties props = new Properties();
            props.load(fis);
            url = props.getProperty("db.postgres.url");
            user = props.getProperty("db.postgres.user");
            password = props.getProperty("db.postgres.password");
        } catch (IOException e) {
            throw new RuntimeException("Could not read DB config", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
