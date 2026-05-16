package com.blockmart.skillmaster.commands;

import com.blockmart.skillmaster.managers.SkillManager;
import com.blockmart.skillmaster.models.SkillType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillCommand implements CommandExecutor {

    private final SkillManager skillManager;

    public SkillCommand(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("level")) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /skill <skillname> level");
            return true;
        }

        String skillName = args[0].toUpperCase();
        SkillType skillType;
        try {
            skillType = SkillType.valueOf(skillName);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid skill type: " + skillName + ". Valid skills: " + SkillType.getNames());
            return true;
        }

        int level = skillManager.getSkillLevel(player.getUniqueId(), skillType);
        player.sendMessage(ChatColor.GREEN + "Your " + skillName + " skill level is: " + level);
        return true;
    }
}
