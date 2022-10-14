package com.tndt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PluginMain extends JavaPlugin {
    public PluginMain() {
        groovyEngine = new GroovyEngine(this);
    }

    @Override
    public void onEnable() {
        groovyEngine.loadAllScripts();
    }
    @Override
    public void onDisable() {
        groovyEngine.unloadAllScripts();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("gx")) {
            return commandGx(sender, command, label, args);
        }
        else if (command.getName().equalsIgnoreCase("gxall")) {
            return commandGxAll(sender, command, label, args);
        }
        else if (command.getName().equalsIgnoreCase("gxunloadall")) {
            return commandGxUnloadAll(sender, command, label, args);
        }
        else if (command.getName().equalsIgnoreCase("gxunload")) {
            return commandGxUnload(sender, command, label, args);
        }
        else if (command.getName().equalsIgnoreCase("gxc")) {
            if (args.length == 0) {
                sender.sendMessage("Too few parameters: first parameter must a script name");
                return false;
            }
            String scriptName = args[0];
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            return groovyEngine.executeCommand(sender, newArgs, scriptName);
        }
        return false;
    }

    boolean commandGx(@NotNull CommandSender sender,
                      @NotNull Command command,
                      @NotNull String label,
                      @NotNull String[] args) {
        if (args.length != 1) {
            sender.sendMessage("gx requires exactly one argument: the name of the script file " +
                "to be loaded. You can specify the name with or without the '.grovy' extension.");
            return false;
        }

        groovyEngine.loadScript(args[0]);
        String scriptName = GroovyEngine.getFullName(args[0]);
        sender.sendMessage("Script " + scriptName + " is loaded.");
        return true;
    }

    boolean commandGxAll(@NotNull CommandSender sender,
                         @NotNull Command command,
                         @NotNull String label,
                         @NotNull String[] args) {
        if (args.length == 0) {
            groovyEngine.loadAllScripts();
            sender.sendMessage("Scripts are loaded.");
            return true;
        } else {
            sender.sendMessage("gxall requires no arguments");
            return false;
        }
    }

    boolean commandGxUnloadAll(@NotNull CommandSender sender,
                               @NotNull Command command,
                               @NotNull String label,
                               @NotNull String[] args) {
        if (args.length == 0) {
            groovyEngine.unloadAllScripts();
            sender.sendMessage("Scripts are unloaded");
            return true;
        } else {
            sender.sendMessage("gxunloadall requires no arguments");
            return false;
        }
    }

    boolean commandGxUnload(@NotNull CommandSender sender,
                            @NotNull Command command,
                            @NotNull String label,
                           @NotNull String[] args) {
        if (args.length == 1) {
            groovyEngine.unloadScript(args[0]);
            String scriptName = GroovyEngine.getFullName(args[0]);
            sender.sendMessage("script " + scriptName + " is unloaded.");
            return true;
        } else {
            sender.sendMessage("gx requires exactly one argument: the name of the script file " +
                    "to be unloaded. You can specify the name with or without the '.grovy' extension.");
            return false;
        }
    }

    private final GroovyEngine groovyEngine;
}