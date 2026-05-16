package com.blockmart.skillmaster.models;

import java.util.UUID;

public class PlayerSkillData {
    private final UUID playerUuid;
    private final SkillType skillType;
    private int experience;
    private int level;

    public PlayerSkillData(UUID playerUuid, SkillType skillType, int experience, int level) {
        this.playerUuid = playerUuid;
        this.skillType = skillType;
        this.experience = experience;
        this.level = level;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public int getExperience() {
        return experience;
    }

    public void addExperience(int amount) {
        if (level < SkillType.MAX_LEVEL) {
            this.experience += amount;
        }
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        if (level < SkillType.MAX_LEVEL) {
            this.level++;
            // Optionally reset experience or carry over overflow
            // For simplicity, we'll let manager handle XP calculations for next level
        }
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }
}
