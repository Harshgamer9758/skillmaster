package com.blockmart.skillmaster.database;

import com.blockmart.skillmaster.SkillMaster;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {

    private final SkillMaster plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(SkillMaster plugin) {
        this.plugin = plugin;
    }

    public void loadDatabase() {
        HikariConfig config = new HikariConfig();
        config.setPoolName("SkillMaster-Hikari");
        config.setMaxLifetime(60000);
        config.setIdleTimeout(30000);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        String databasePath = new File(plugin.getDataFolder(), "skills.db").getAbsolutePath();
        config.setJdbcUrl("jdbc:sqlite:" + databasePath);

        try {
            this.dataSource = new HikariDataSource(config);
            createTables();
            plugin.getLogger().log(Level.INFO, "SQLite database connected and tables checked.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to SQLite database: " + e.getMessage(), e);
        }
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS player_skills (" +
                     "player_uuid VARCHAR(36) NOT NULL," +
                     "skill_type VARCHAR(50) NOT NULL," +
                     "experience INTEGER NOT NULL DEFAULT 0," +
                     "level INTEGER NOT NULL DEFAULT 1," +
                     "PRIMARY KEY (player_uuid, skill_type)" +
                     ");";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public CompletableFuture<Void> executeUpdateAsync(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error executing update: " + sql, e);
            }
        });
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().log(Level.INFO, "SQLite database connection closed.");
        }
    }
}
