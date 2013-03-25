package com.pwn9.PwnFilter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.Listener;

/**
* A Regular Expression (REGEX) Chat Filter For Bukkit with many great features
* Handle events for all Player related events
* @author tremor77
*/

public class PwnFilterCommandListenerHigh implements Listener {
    private final PwnFilter plugin;
	public PwnFilterCommandListenerHigh(PwnFilter plugin) {
	    plugin.getServer().getPluginManager().registerEvents(this, plugin);    
	    this.plugin = plugin;
	}
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    	plugin.filterCommand(event);
    }  
}