package com.tndt;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface ScriptCommandExecutor {
    boolean onCommand(@NotNull CommandSender sender,
                      @NotNull String commandName,
                      @NotNull String[] args,
                      @NotNull String scriptName);
    //void addCommands();
}
