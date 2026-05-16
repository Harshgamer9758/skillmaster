package com.blockmart.skillmaster.managers;

import com.blockmart.skillmaster.SkillMaster;
import com.blockmart.skillmaster.database.DatabaseManager;
import com.blockmart.skillmaster.models.PlayerSkillData;
import com.blockmart.skillmaster.models.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SkillManager {

    private final SkillMaster plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Map<SkillType, PlayerSkillData>> playerSkillCache;

    private static final int BASE_XP_REQUIRED = 100;
    private static final double XP_MULTIPLIER = 1.2;

    public SkillManager(SkillMaster plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.playerSkillCache = new ConcurrentHashMap<>();
    }

    public void loadPlayerSkillsAsync(UUID playerUuid) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<SkillType, PlayerSkillData> skills = new EnumMap<>(SkillType.class);
                String sql = "SELECT skill_type, experience, level FROM player_skills WHERE player_uuid = ?";
                try (Connection conn = databaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, playerUuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        SkillType type = SkillType.valueOf(rs.getString("skill_type"));
                        int exp = rs.getInt("experience");
                        int level = rs.getInt("level");
                        skills.put(type, new PlayerSkillData(playerUuid, type, exp, level));
                    }
                    playerSkillCache.put(playerUuid, skills);
                    // Initialize any missing skills
                    for (SkillType type : SkillType.values()) {
                        skills.computeIfAbsent(type, k -> {
                            PlayerSkillData data = new PlayerSkillData(playerUuid, k, 0, 1);
                            saveSkillData(data);
                            return data;
                        });
                    }
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to load skills for player " + playerUuid, e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void addExperience(UUID playerUuid, SkillType skillType, int amount) {
        PlayerSkillData data = playerSkillCache.computeIfAbsent(playerUuid, k -> new EnumMap<>(SkillType.class))
                .computeIfAbsent(skillType, k -> {
                    PlayerSkillData newData = new PlayerSkillData(playerUuid, k, 0, 1);
                    saveSkillData(newData); // Save initial if not present
                    return newData;
                });

        data.addExperience(amount);

        int currentLevel = data.getLevel();
        int xpToNextLevel = getExperienceRequiredForLevel(currentLevel + 1);

        while (data.getExperience() >= xpToNextLevel) {
            data.levelUp();
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.GOLD + "Your " + skillType.getName() + " skill has leveled up to level " + data.getLevel() + "!");
            }
            xpToNextLevel = getExperienceRequiredForLevel(data.getLevel() + 1);
             if (data.getLevel() >= SkillType.MAX_LEVEL) {
                // Player reached max level, no further level ups possible
                data.setExperience(xpToNextLevel - 1); // Cap XP at just under max level requirement
                break;
            }
        }
        saveSkillData(data);
    }

    public int getSkillLevel(UUID playerUuid, SkillType skillType) {
        PlayerSkillData data = playerSkillCache.getOrDefault(playerUuid, new EnumMap<>(SkillType.class))
                .getOrDefault(skillType, new PlayerSkillData(playerUuid, skillType, 0, 1));
        return data.getLevel();
    }

    public int getSkillExperience(UUID playerUuid, SkillType skillType) {
        PlayerSkillData data = playerSkillCache.getOrDefault(playerUuid, new EnumMap<>(SkillType.class))
                .getOrDefault(skillType, new PlayerSkillData(playerUuid, skillType, 0, 1));
        return data.getExperience();
    }

    public int getExperienceRequiredForLevel(int level) {
        if (level <= 1) return BASE_XP_REQUIRED;
        return (int) Math.round(BASE_XP_REQUIRED * Math.pow(XP_MULTIPLIER, level - 1));
    }

    private void saveSkillData(PlayerSkillData data) {
        String sql = "INSERT INTO player_skills (player_uuid, skill_type, experience, level) VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT(player_uuid, skill_type) DO UPDATE SET experience=excluded.experience, level=excluded.level;";
        databaseManager.executeUpdateAsync(sql,
                data.getPlayerUuid().toString(),
                data.getSkillType().toString(),
                data.getExperience(),
                data.getLevel()
        );
    }
}
