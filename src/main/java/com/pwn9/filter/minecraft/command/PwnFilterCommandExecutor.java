package com.pwn9.filter.minecraft.command;

import com.pwn9.filter.engine.api.CommandSender;

import java.util.List;

/**
 * Created for the Charlton IT Project.
 * Created by Narimm on 19/08/2020.
 */
public interface PwnFilterCommandExecutor {

    boolean onCommand(CommandSender sender, String command, String... args);

    List<String> onTabComplete(CommandSender sender, String command, String... args);
}
