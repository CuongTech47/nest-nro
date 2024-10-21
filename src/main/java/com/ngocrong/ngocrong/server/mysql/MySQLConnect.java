package com.ngocrong.ngocrong.server.mysql;

import com.ngocrong.ngocrong.server.Server;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnect {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static HikariDataSource dataSource;

    public static synchronized void initialize(String host, int port, String database, String user, String pass) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", host, port, database));
        config.setUsername(user);
        config.setPassword(pass);

        // Cấu hình các thuộc tính khác (tuỳ chọn)
        config.setMaximumPoolSize(10); // Số lượng kết nối tối đa trong pool
        config.setConnectionTimeout(30000); // Thời gian chờ kết nối (30 giây)
        config.setIdleTimeout(600000); // Thời gian chờ tối đa của kết nối không sử dụng (10 phút)
        config.setMaxLifetime(1800000); // Thời gian sống tối đa của kết nối (30 phút)

        dataSource = new HikariDataSource(config);
        logger.debug("HikariCP initialized successfully");
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            logger.error("DataSource not initialized. Call initialize() method first.");
            throw new IllegalStateException("DataSource not initialized");
        }
        return dataSource.getConnection();
    }

    public static synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
            logger.debug("DataSource closed successfully");
        }
    }

}
