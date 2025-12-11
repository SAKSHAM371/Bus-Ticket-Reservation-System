package com.bus.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DButil {

    private static String url = "jdbc:mysql://localhost:3306/busreservation?useSSL=false&serverTimezone=UTC";
    private static String username = "javauser";
    private static String password = "java123";

    static {
        try {
            
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found. Make sure mysql-connector-java jar is on the classpath.");
            e.printStackTrace();
        }

        Properties props = new Properties();
        InputStream in = null;
        try {
            in = DButil.class.getClassLoader().getResourceAsStream("config.properties");
            if (in == null) {
                File f = new File("config.properties");
                if (f.exists()) in = new FileInputStream(f);
            }
            if (in != null) {
                props.load(in);
                String u = props.getProperty("db.url");
                String usr = props.getProperty("db.username");
                String pwd = props.getProperty("db.password");

                if (u != null && !u.trim().isEmpty()) url = u.trim();
                if (usr != null && !usr.trim().isEmpty()) username = usr.trim();
                if (pwd != null) password = pwd;
            }
        } catch (IOException e) {
            System.err.println("Warning: could not load config.properties (" + e.getMessage() + "). Using defaults.");
        } finally {
            if (in != null) try { in.close(); } catch (IOException ignored) {}
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    
    public static Connection provideConnection() throws SQLException {
        return getConnection();
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try { r.close(); } catch (Exception ignored) {}
            }
        }
    }

    
    public static String getUrl() { return url; }
    public static String getUsername() { return username; }
}

// private static String url = "jdbc:mysql://localhost:3306/busreservation?useSSL=false&serverTimezone=UTC";
//     private static String username = "javauser";
//     private static String password = "java123";