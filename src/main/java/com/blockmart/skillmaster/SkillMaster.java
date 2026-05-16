package com.blockmart.skillmaster;

import com.blockmart.skillmaster.commands.SkillCommand;
import com.blockmart.skillmaster.database.DatabaseManager;
import com.blockmart.skillmaster.listeners.PlayerActionListener;
import com.blockmart.skillmaster.managers.SkillManager;
import com.blockmart.skillmaster.models.SkillType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class SkillMaster extends JavaPlugin {

    private static SkillMaster instance;
    private DatabaseManager databaseManager;
    private SkillManager skillManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.loadDatabase();

        this.skillManager = new SkillManager(this, databaseManager);

        registerCommands();
        registerListeners();

        getLogger().log(Level.INFO, "SkillMaster has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        getLogger().log(Level.INFO, "SkillMaster has been disabled!");
    }

    private void registerCommands() {
        getCommand("skill").setExecutor(new SkillCommand(skillManager));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerActionListener(skillManager), this);
    }

    public static SkillMaster getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }
}
