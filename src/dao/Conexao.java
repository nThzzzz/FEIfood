package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexao {

    public Connection getConnection() throws SQLException {
        
        String url = "jdbc:postgresql://ep-billowing-bonus-ahdeh7gy-pooler.c-3.us-east-1.aws.neon.tech:5432/neondb";

        Properties props = new Properties();
        
        props.setProperty("user", "neondb_owner"); 
        props.setProperty("password", "npg_Sp4mPlGLYrW1"); 
        
        props.setProperty("sslmode", "require");
        props.setProperty("channelBinding", "require");

        Connection conn = DriverManager.getConnection(url, props);
        return conn;
    }
}