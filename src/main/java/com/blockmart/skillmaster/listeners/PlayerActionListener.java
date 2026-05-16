package com.blockmart.skillmaster.listeners;

import com.blockmart.skillmaster.managers.SkillManager;
import com.blockmart.skillmaster.models.SkillType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Level;

public class PlayerActionListener implements Listener {

    private final SkillManager skillManager;

    public PlayerActionListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        skillManager.loadPlayerSkillsAsync(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block brokenBlock = event.getBlock();

        // Example: Grant XP for mining custom ore (replace with actual NBT filtering if needed)
        if (brokenBlock.getType() == Material.STONE || brokenBlock.getType() == Material.COAL_ORE) {
            skillManager.addExperience(player.getUniqueId(), SkillType.MINING, 5);
        }
    }
}
