package custom.orm.db.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnector {
    private Properties properties = new Properties();
    public static String PROPERTIES_PATH = "C:\\Users\\Public\\Documents\\db.properties";

    public DBConnector(String propertiesFilePath) throws IOException {
        loadProperties(propertiesFilePath);
    }

    private void loadProperties(String filePath) throws IOException {
        try (FileInputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        }
    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        String driver = properties.getProperty("db.driver");

        Class.forName(driver);
        Connection c = DriverManager.getConnection(url, username, password);

        c.setAutoCommit(false);

        return c;
    }
    
}