package com.pwn9.filter.minecraft.command;

import com.pwn9.filter.bukkit.config.BukkitConfig;
import com.pwn9.filter.engine.api.CommandSender;
import com.pwn9.filter.minecraft.api.MinecraftAPI;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created for the Charlton IT Project.
 * Created by Narimm on 19/08/2020.
 */
public class PwnFilterMute implements PwnFilterCommandExecutor {

    private MinecraftAPI api;
    private Logger logger;

    @Override
    public boolean onCommand(CommandSender sender, String command, String... args) {
        if (api.globalMute()) {
            api.sendBroadcast(ChatColor.RED + "Global mute cancelled by " + sender.getName());
            logger.info("global mute cancelled by " + sender.getName());
            BukkitConfig.setGlobalMute(false);
        } else {
            api.sendBroadcast(ChatColor.RED + "Global mute initiated by " + sender.getName());
            logger.info("global mute initiated by " + sender.getName());
            BukkitConfig.setGlobalMute(true);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String command, String... args) {
        return Collections.emptyList();
    }
}
